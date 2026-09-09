import { describe, expect, it } from 'vitest'
import { consumptionPayWay, money, minutesText, rechargePayWay } from './formatters'

describe('shared formatters', () => {
  it('formats money and payment labels consistently', () => {
    expect(money('12.5')).toBe('12.50')
    expect(rechargePayWay(4)).toBe('银行卡')
    expect(consumptionPayWay(2)).toBe('会员余额')
  })

  it('formats table usage minutes', () => {
    expect(minutesText(65)).toBe('1小时5分钟')
    expect(minutesText(30)).toBe('30分钟')
  })
})
