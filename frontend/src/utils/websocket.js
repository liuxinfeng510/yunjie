import { ref, onUnmounted } from 'vue'

/**
 * WebSocket 工具类
 * 用于连接后端 STOMP 端点并订阅消息
 */
class WebSocketClient {
  constructor() {
    this.socket = null
    this.stompClient = null
    this.connected = false
    this.subscriptions = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
  }

  /**
   * 连接 WebSocket
   */
  connect() {
    return new Promise((resolve, reject) => {
      // 动态导入 SockJS 和 STOMP（前端需安装 sockjs-client 和 @stomp/stompjs）
      // 由于这些库可能未安装，这里使用原生 WebSocket 作为备选
      const wsUrl = `${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/api/ws`
      
      try {
        this.socket = new WebSocket(wsUrl)
        
        this.socket.onopen = () => {
          this.connected = true
          this.reconnectAttempts = 0
          console.log('WebSocket 已连接')
          resolve()
        }
        
        this.socket.onclose = (event) => {
          this.connected = false
          console.log('WebSocket 已断开', event.code, event.reason)
          this.handleReconnect()
        }
        
        this.socket.onerror = (error) => {
          console.error('WebSocket 错误', error)
          reject(error)
        }
        
        this.socket.onmessage = (event) => {
          this.handleMessage(event.data)
        }
      } catch (error) {
        reject(error)
      }
    })
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    this.connected = false
    this.subscriptions.clear()
  }

  /**
   * 订阅主题
   */
  subscribe(topic, callback) {
    if (!this.subscriptions.has(topic)) {
      this.subscriptions.set(topic, new Set())
    }
    this.subscriptions.get(topic).add(callback)
    
    return () => {
      const callbacks = this.subscriptions.get(topic)
      if (callbacks) {
        callbacks.delete(callback)
        if (callbacks.size === 0) {
          this.subscriptions.delete(topic)
        }
      }
    }
  }

  /**
   * 发送消息
   */
  send(destination, data) {
    if (this.connected && this.socket) {
      this.socket.send(JSON.stringify({ destination, body: data }))
    }
  }

  /**
   * 处理收到的消息
   */
  handleMessage(data) {
    try {
      const message = JSON.parse(data)
      const topic = message.topic || message.destination
      const callbacks = this.subscriptions.get(topic)
      if (callbacks) {
        callbacks.forEach(cb => cb(message.body || message))
      }
    } catch (e) {
      console.error('消息解析失败', e)
    }
  }

  /**
   * 重连处理
   */
  handleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`WebSocket 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      setTimeout(() => this.connect(), this.reconnectDelay)
    }
  }
}

// 全局单例
let wsInstance = null

export function getWebSocketClient() {
  if (!wsInstance) {
    wsInstance = new WebSocketClient()
  }
  return wsInstance
}

/**
 * Vue Composable: useWebSocket
 * 在组件中使用 WebSocket
 */
export function useWebSocket() {
  const client = getWebSocketClient()
  const connected = ref(client.connected)
  const unsubscribes = []

  const connect = async () => {
    if (!client.connected) {
      await client.connect()
    }
    connected.value = client.connected
  }

  const subscribe = (topic, callback) => {
    const unsub = client.subscribe(topic, callback)
    unsubscribes.push(unsub)
    return unsub
  }

  const send = (destination, data) => {
    client.send(destination, data)
  }

  onUnmounted(() => {
    unsubscribes.forEach(unsub => unsub())
  })

  return {
    connected,
    connect,
    subscribe,
    send
  }
}
