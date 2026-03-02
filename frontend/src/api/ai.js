import request from '@/utils/request'

// 中药图片识别
export function recognizeHerb(data) {
  return request.post('/ai/herb/recognize', data)
}

// 中药称重识别
export function recognizeHerbWeighing(data) {
  return request.post('/ai/herb/recognize-weighing', data)
}

// 处方单OCR
export function ocrPrescription(data) {
  return request.post('/ai/ocr/prescription', data)
}

// 发票OCR
export function ocrInvoice(data) {
  return request.post('/ai/ocr/invoice', data)
}

// 药品包装OCR
export function ocrDrugPackage(data) {
  return request.post('/ai/ocr/drug-package', data)
}

// 销售助手对话
export function salesChat(question) {
  return request.post('/ai/sales-assistant/chat', { question })
}

// 症状药品推荐
export function recommendBySymptoms(symptoms, patientInfo) {
  return request.post('/ai/sales-assistant/recommend', { symptoms, patientInfo })
}

// 药品搭配建议
export function suggestCombination(drugs, symptoms) {
  return request.post('/ai/sales-assistant/combination', { drugs, symptoms })
}

// 用药指导
export function getMedicationGuidance(drugName, patientCondition) {
  return request.post('/ai/sales-assistant/guidance', { drugName, patientCondition })
}

// 视觉结算识别
export function visionCheckout(data) {
  return request.post('/ai/vision-checkout/recognize', data)
}
