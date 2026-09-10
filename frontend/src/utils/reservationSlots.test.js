import { describe, expect, it } from 'vitest'
import { buildTodayStartSlots, overlaps, reservationTimeRange } from './reservationSlots'

describe('reservationSlots', () => {
  const now = new Date(2026, 8, 10, 10, 10, 0)
  const table = {
    status: 0,
    todayReservations: [{ startTime: '2026-09-10T14:00:00', endTime: '2026-09-10T16:00:00' }]
  }

  it('disables past and conflicting start times', () => {
    const slots = buildTodayStartSlots(table, 1, now)
    expect(slots.find((slot) => slot.label === '10:00').disabled).toBe(true)
    expect(slots.find((slot) => slot.label === '13:30').reason).toBe('与已有预约冲突')
    expect(slots.find((slot) => slot.label === '16:00').disabled).toBe(false)
  })

  it('considers the selected duration and table status', () => {
    expect(buildTodayStartSlots(table, 2, now).find((slot) => slot.label === '12:30').disabled).toBe(true)
    expect(buildTodayStartSlots({ ...table, status: 2 }, 1, now).every((slot) => slot.disabled)).toBe(true)
  })

  it('uses half-open overlap boundaries and formats ranges', () => {
    expect(overlaps(new Date(2026, 8, 10, 13), new Date(2026, 8, 10, 14), table.todayReservations[0])).toBe(false)
    expect(reservationTimeRange(table.todayReservations[0])).toBe('14:00–16:00')
  })
})
