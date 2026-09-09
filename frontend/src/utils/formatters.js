export function money(value) {
  return Number(value || 0).toFixed(2)
}

export function rechargePayWay(value) {
  return ['', '现金', '微信', '支付宝', '银行卡'][value] || '未知'
}

export function consumptionPayWay(value) {
  return ['', '现金', '会员余额', '挂账'][value] || '未知'
}

export function minutesText(value) {
  const minutes = Number(value || 0)
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  return hours ? `${hours}小时${rest ? `${rest}分钟` : ''}` : `${rest}分钟`
}
