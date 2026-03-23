/**
 * 小票打印共享工具
 * 从 POS.vue 提取的纯函数，供 POS 和 OrderList 等页面复用
 */

/**
 * 支付方式中文标签
 */
export function getPaymentLabel(method) {
  const labels = {
    'CASH': '现金',
    'WECHAT': '微信',
    'ALIPAY': '支付宝',
    'MEDICAL_INSURANCE': '医保'
  }
  return labels[method] || method
}

/**
 * 生成普通销售小票 HTML
 * @param {Object} data - 小票数据
 * @param {Object} fields - 字段显示配置
 * @param {string} paperWidth - 纸张宽度 '58' 或 '80'
 */
export function generateReceiptHtml(data, fields, paperWidth) {
  const is58 = paperWidth === '58'
  const bodyWidth = is58 ? '58mm' : '80mm'
  const bodyPadding = is58 ? '3mm' : '5mm'
  const baseFontSize = is58 ? '11px' : '12px'
  const shopNameSize = is58 ? '14px' : '16px'
  const tenantNameSize = is58 ? '11px' : '12px'
  const totalFontSize = is58 ? '18px' : '22px'
  const mfgMaxLen = is58 ? 12 : 20

  // -- 头部 --
  let headerHtml = ''
  if (fields.header?.shopName) {
    headerHtml += `<div class="shop-name" style="font-size:${shopNameSize};">${data.shopName}</div>`
  }
  if (fields.header?.tenantName && data.tenantName && data.tenantName !== data.shopName && !data.shopName.includes(data.tenantName) && !data.tenantName.includes(data.shopName)) {
    headerHtml += `<div style="font-size:${tenantNameSize};color:#444;margin-top:2px;">${data.tenantName}</div>`
  }
  if (data.storeAddress) {
    headerHtml += `<div style="font-size:${is58 ? '10px' : '11px'};color:#333;margin-top:2px;">地址：${data.storeAddress}</div>`
  }
  if (data.storePhone) {
    headerHtml += `<div style="font-size:${is58 ? '10px' : '11px'};color:#333;margin-top:1px;">电话：${data.storePhone}</div>`
  }

  // -- 订单信息 --
  let orderInfoHtml = ''
  if (fields.orderInfo?.orderNo) {
    orderInfoHtml += `<div class="info-row"><span>单号:</span><span>${data.orderNo}</span></div>`
  }
  if (fields.orderInfo?.dateTime) {
    orderInfoHtml += `<div class="info-row"><span>时间:</span><span>${data.dateTime}</span></div>`
  }
  if (fields.orderInfo?.cashier) {
    orderInfoHtml += `<div class="info-row"><span>收银员:</span><span>${data.cashierName}</span></div>`
  }

  // -- 会员信息 --
  let memberInfoHtml = ''
  if (data.member) {
    const parts = []
    if (fields.memberInfo?.memberName) parts.push(data.member.name)
    if (fields.memberInfo?.memberPhone) parts.push(data.member.phone)
    if (parts.length > 0) {
      memberInfoHtml = `<div class="info-row"><span>会员:</span><span>${parts.join(' ')}</span></div>`
    }
    if (fields.memberInfo?.memberPoints !== false) {
      memberInfoHtml += `<div class="info-row"><span>积分余额:</span><span>${data.member.points} 分</span></div>`
    }
  }

  // -- 商品明细 --
  const showSpec = fields.itemDetail?.specification !== false
  const showBatch = fields.itemDetail?.batchNo !== false
  const showMfg = fields.itemDetail?.manufacturer !== false
  const showUnitPrice = fields.itemDetail?.unitPrice !== false

  let theadCols = ''
  if (showSpec) theadCols += '<td>规格</td>'
  if (showBatch) theadCols += '<td>批号</td>'
  theadCols += '<td style="text-align:right;">数量</td><td style="text-align:right;">金额</td>'

  const colCount = (showSpec ? 1 : 0) + (showBatch ? 1 : 0) + 2

  const itemsHtml = data.items.map(item => {
    let rows = ''
    if (fields.itemDetail?.drugName !== false) {
      rows += `<tr><td colspan="${colCount}" style="text-align:left;padding:2px 0;border-bottom:1px dashed #ccc;">${item.name}</td></tr>`
    }
    let detailCols = ''
    if (showSpec) detailCols += `<td style="font-size:${baseFontSize};color:#000;">${item.specification}</td>`
    if (showBatch) detailCols += `<td style="font-size:${baseFontSize};color:#000;">${item.batchNo}</td>`
    detailCols += `<td style="text-align:right;">${item.quantity}</td>`
    detailCols += `<td style="text-align:right;">¥${item.amount}</td>`
    rows += `<tr>${detailCols}</tr>`
    const extraParts = []
    if (showMfg && item.manufacturer) {
      const mfg = item.manufacturer.length > mfgMaxLen ? item.manufacturer.substring(0, mfgMaxLen) + '...' : item.manufacturer
      extraParts.push(mfg)
    }
    if (showUnitPrice) extraParts.push('单价:¥' + item.unitPrice)
    if (extraParts.length > 0) {
      rows += `<tr><td colspan="${colCount}" style="font-size:${is58 ? '10px' : '11px'};color:#000;padding-bottom:4px;">${extraParts.join(' | ')}</td></tr>`
    }
    return rows
  }).join('')

  // -- 汇总区域 --
  let summaryHtml = ''
  if (fields.summary?.itemCount !== false) {
    summaryHtml += `<div class="total-row"><span>商品数量:</span><span>${data.itemCount} 件</span></div>`
  }
  if (fields.summary?.totalAmount !== false) {
    summaryHtml += `<div class="total-row"><span>商品金额:</span><span>¥${data.totalAmount}</span></div>`
  }
  if (fields.summary?.memberDiscount !== false && data.memberPriceSaving > 0) {
    summaryHtml += `<div class="total-row"><span>会员价优惠:</span><span>-¥${data.memberPriceSaving.toFixed(2)}</span></div>`
  }
  if (fields.summary?.wholeDiscount !== false && data.enableWholeOrderDiscount && data.wholeOrderDiscountSaving > 0) {
    summaryHtml += `<div class="total-row"><span>整单折扣(${(data.memberDiscountRate * 10).toFixed(1)}折):</span><span>-¥${data.wholeOrderDiscountSaving.toFixed(2)}</span></div>`
  }
  if (fields.summary?.manualDiscount !== false && data.discountAmount > 0) {
    summaryHtml += `<div class="total-row"><span>手动优惠:</span><span>-¥${data.discountAmount.toFixed(2)}</span></div>`
  }
  if (fields.summary?.payMethod !== false) {
    summaryHtml += `<div class="total-row"><span>支付方式:</span><span>${data.paymentLabel}</span></div>`
  }
  if (fields.summary?.cashInfo !== false && data.paymentMethod === 'CASH' && data.cashReceived > 0) {
    summaryHtml += `<div class="total-row"><span>收取现金:</span><span>¥${data.cashReceived.toFixed(2)}</span></div>`
    summaryHtml += `<div class="total-row"><span>找零:</span><span>¥${data.changeAmount.toFixed(2)}</span></div>`
  }
  summaryHtml += `<div class="total-row total-amount"><span>实付金额:</span><span style="color:#000;font-weight:900;">¥${data.payAmount}</span></div>`

  // -- 页脚 --
  let footerHtml = ''
  if (fields.footer?.footerText !== false) {
    const lines = data.footerText.split(/\\n|\n/).map(l => `<p>${l}</p>`).join('')
    footerHtml = `<div class="footer">${lines}</div>`
  }

  return `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>销售小票</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: 'Microsoft YaHei', 'SimHei', sans-serif; width: 100%; max-width: ${bodyWidth}; padding: ${bodyPadding}; font-size: ${baseFontSize}; color: #000; margin: 0 auto; }
    .header { text-align: center; margin-bottom: 10px; }
    .shop-name { font-weight: bold; }
    .divider { border-top: 1px dashed #000; margin: 8px 0; }
    .info-row { display: flex; justify-content: space-between; margin: 3px 0; }
    .items-table { width: 100%; border-collapse: collapse; margin: 8px 0; table-layout: fixed; }
    .items-table td { padding: 2px 0; vertical-align: top; word-break: break-all; }
    .total-section { margin-top: 10px; }
    .total-row { display: flex; justify-content: space-between; margin: 4px 0; }
    .total-amount { font-size: ${totalFontSize}; font-weight: bold; }
    .footer { text-align: center; margin-top: 15px; font-size: ${is58 ? '11px' : '12px'}; color: #000; }
    .footer p { margin-top: 3px; }
    @page { size: ${bodyWidth} auto; margin: 0mm; }
    @media print { body { width: 100%; max-width: none; margin: 0; padding: ${bodyPadding}; } }
  </style>
</head>
<body>
  ${headerHtml ? `<div class="header">${headerHtml}</div>` : ''}
  <div class="divider"></div>
  ${orderInfoHtml}${memberInfoHtml}
  <div class="divider"></div>
  <table class="items-table">
    <thead><tr style="font-weight:bold;border-bottom:1px solid #000;">${theadCols}</tr></thead>
    <tbody>${itemsHtml}</tbody>
  </table>
  <div class="divider"></div>
  <div class="total-section">${summaryHtml}</div>
  <div class="divider"></div>
  ${footerHtml}
</body>
</html>`
}

/**
 * 生成中药处方笺 HTML
 * @param {Object} data - 处方笺数据
 * @param {Object} fields - 字段显示配置
 * @param {string} paperWidth - 纸张宽度 '58' 或 '80'
 */
export function generateHerbReceiptHtml(data, fields, paperWidth) {
  const is58 = paperWidth === '58'
  const bodyWidth = is58 ? '58mm' : '80mm'
  const bodyPadding = is58 ? '3mm' : '5mm'
  const baseFontSize = is58 ? '12px' : '13px'
  const shopNameSize = is58 ? '14px' : '16px'
  const tenantNameSize = is58 ? '11px' : '12px'
  const totalFontSize = is58 ? '16px' : '20px'

  let headerHtml = ''
  if (fields.header?.shopName !== false) {
    headerHtml += `<div class="shop-name" style="font-size:${shopNameSize};">${data.shopName}</div>`
  }
  if (fields.header?.tenantName !== false && data.tenantName && data.tenantName !== data.shopName && !data.shopName.includes(data.tenantName) && !data.tenantName.includes(data.shopName)) {
    headerHtml += `<div style="font-size:${tenantNameSize};color:#444;margin-top:2px;">${data.tenantName}</div>`
  }
  if (data.storeAddress) {
    headerHtml += `<div style="font-size:${is58 ? '10px' : '11px'};color:#333;margin-top:2px;">地址：${data.storeAddress}</div>`
  }
  if (data.storePhone) {
    headerHtml += `<div style="font-size:${is58 ? '10px' : '11px'};color:#333;margin-top:1px;">电话：${data.storePhone}</div>`
  }
  headerHtml += `<div style="font-size:${is58 ? '14px' : '16px'};font-weight:bold;margin-top:4px;">中药处方笺</div>`

  // -- 订单信息（与西药小票一致的 flex 布局）--
  let orderInfoHtml = ''
  if (fields.orderInfo?.orderNo !== false) {
    orderInfoHtml += `<div class="info-row"><span>单号:</span><span>${data.orderNo}</span></div>`
  }
  if (fields.orderInfo?.dateTime !== false) {
    orderInfoHtml += `<div class="info-row"><span>时间:</span><span>${data.dateTime}</span></div>`
  }
  if (fields.orderInfo?.cashier !== false) {
    orderInfoHtml += `<div class="info-row"><span>收银员:</span><span>${data.cashierName}</span></div>`
  }

  // -- 会员信息 --
  let memberInfoHtml = ''
  if (data.member) {
    const parts = []
    if (fields.memberInfo?.memberName !== false) parts.push(data.member.name)
    if (fields.memberInfo?.memberPhone !== false) parts.push(data.member.phone)
    if (parts.length > 0) {
      memberInfoHtml += `<div class="info-row"><span>会员:</span><span>${parts.join(' ')}</span></div>`
    }
    if (fields.memberInfo?.memberPoints !== false) {
      memberInfoHtml += `<div class="info-row"><span>积分余额:</span><span>${data.member.points} 分</span></div>`
    }
  }

  const itemsHtml = data.items
    .map(
      (item) => `
    <tr>
      <td style="padding:3px 2px;">${item.name}</td>
      <td style="text-align:right;padding:3px 2px;">${item.dosePerGram}</td>
      <td style="text-align:right;padding:3px 2px;">${item.totalGram}</td>
      <td style="text-align:right;padding:3px 2px;">${item.unitPrice.toFixed(2)}</td>
      <td style="text-align:right;padding:3px 2px;">${item.amount}</td>
    </tr>`
    )
    .join('')

  let summaryHtml = ''
  summaryHtml += `<div class="sum-row"><span>每剂总重:</span><span>${data.perDoseTotalWeight}g</span></div>`
  summaryHtml += `<div class="sum-row"><span>${data.doseCount}副总重:</span><span>${data.totalWeight}g</span></div>`
  summaryHtml += `<div class="sum-row"><span>商品金额:</span><span>¥${data.herbTotal}</span></div>`
  if (data.discountAmount > 0) {
    summaryHtml += `<div class="sum-row"><span>优惠:</span><span>-¥${data.discountAmount.toFixed(2)}</span></div>`
  }
  summaryHtml += `<div class="sum-row"><span>支付方式:</span><span>${data.paymentLabel}</span></div>`
  if (data.paymentMethod === 'CASH' && data.cashReceived > 0) {
    summaryHtml += `<div class="sum-row"><span>收取现金:</span><span>¥${data.cashReceived.toFixed(2)}</span></div>`
    summaryHtml += `<div class="sum-row"><span>找零:</span><span>¥${data.changeAmount.toFixed(2)}</span></div>`
  }
  summaryHtml += `<div class="sum-row total-amount"><span>实付金额:</span><span>¥${data.payAmount}</span></div>`

  return `<!DOCTYPE html><html><head><meta charset="utf-8">
<style>
  @page { size: ${bodyWidth} auto; margin: 0mm; }
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: 'Microsoft YaHei','SimHei',sans-serif; font-size: ${baseFontSize}; color: #000; margin: 0 auto; padding: ${bodyPadding}; width: 100%; max-width: ${bodyWidth}; -webkit-print-color-adjust: exact; }
  .header { text-align: center; margin-bottom: 10px; }
  .shop-name { font-weight: bold; }
  .divider { border-top: 1px dashed #000; margin: 8px 0; }
  .info-row { display: flex; justify-content: space-between; margin: 3px 0; }
  .dose-label { text-align: center; font-size: ${is58 ? '15px' : '17px'}; font-weight: 900; padding: 6px 0; }
  .items-table { width: 100%; border-collapse: collapse; font-size: ${is58 ? '11px' : '12px'}; }
  .items-table th, .items-table td { padding: 3px 2px; }
  .items-table th { border-bottom: 1px solid #000; text-align: left; font-weight: 700; }
  .items-table th:nth-child(n+2), .items-table td:nth-child(n+2) { text-align: right; }
  .sum-row { display: flex; justify-content: space-between; margin: 3px 0; font-size: ${baseFontSize}; }
  .total-amount { font-size: ${totalFontSize}; font-weight: 900; }
  .footer { text-align: center; margin-top: 15px; font-size: ${is58 ? '11px' : '12px'}; color: #000; }
  .footer p { margin-top: 3px; }
  @media print { body { width: 100%; max-width: none; margin: 0; padding: ${bodyPadding}; } }
</style></head><body>
<div class="header">${headerHtml}</div>
<div class="divider"></div>
${orderInfoHtml}${memberInfoHtml}
<div class="divider"></div>
<div class="dose-label">${data.doseCount} 副</div>
<div class="divider"></div>
<table class="items-table">
  <tr><th>药名</th><th>克/剂</th><th>总重</th><th>单价</th><th>小计</th></tr>
  ${itemsHtml}
</table>
<div class="divider"></div>
${summaryHtml}
<div class="divider"></div>
<div class="footer">${data.footerText.split(/\\n|\n/).map(l => '<p>' + l + '</p>').join('')}</div>
</body></html>`
}

/**
 * 通过隐藏 iframe 执行打印
 */
export function printViaIframe(html) {
  document.querySelectorAll('iframe[data-print-frame]').forEach(el => {
    if (el.parentNode) el.parentNode.removeChild(el)
  })

  const iframe = document.createElement('iframe')
  iframe.setAttribute('data-print-frame', '1')
  iframe.style.cssText = 'position:fixed;left:-9999px;top:-9999px;width:80mm;height:1px;border:none;visibility:hidden;'
  document.body.appendChild(iframe)

  iframe.onload = () => {
    setTimeout(() => {
      try {
        iframe.contentWindow.focus()
        iframe.contentWindow.print()
      } catch (e) { /* ignore */ }
    }, 100)
  }

  iframe.contentWindow.onafterprint = () => {
    if (iframe.parentNode) document.body.removeChild(iframe)
  }

  const doc = iframe.contentDocument || iframe.contentWindow.document
  doc.open()
  doc.write(html)
  doc.close()

  setTimeout(() => {
    if (iframe.parentNode) document.body.removeChild(iframe)
  }, 3000)
}

/**
 * 默认小票字段配置
 */
export const DEFAULT_RECEIPT_FIELDS = {
  header: { shopName: true, tenantName: true, subtitle: true },
  orderInfo: { orderNo: true, dateTime: true, cashier: true },
  memberInfo: { memberName: true, memberPhone: true, memberPoints: true },
  itemDetail: { drugName: true, specification: true, batchNo: true, manufacturer: true, unitPrice: true },
  summary: { itemCount: true, totalAmount: true, memberDiscount: true, wholeDiscount: true, manualDiscount: true, payMethod: true, cashInfo: true },
  footer: { footerText: true }
}
