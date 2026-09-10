const pad = (value) => String(value).padStart(2, '0')

export function parseLocalDateTime(value) {
  if (!value) return null
  return new Date(value.replace(' ', 'T'))
}

export function toApiDateTime(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:00`
}

export function timeLabel(value) {
  const date = value instanceof Date ? value : parseLocalDateTime(value)
  return date ? `${pad(date.getHours())}:${pad(date.getMinutes())}` : '--'
}

export function reservationTimeRange(reservation) {
  return `${timeLabel(reservation.startTime)}–${timeLabel(reservation.endTime)}`
}

export function overlaps(start, end, reservation) {
  const reservedStart = parseLocalDateTime(reservation.startTime)
  const reservedEnd = parseLocalDateTime(reservation.endTime)
  return reservedStart && reservedEnd && start < reservedEnd && end > reservedStart
}

export function buildTodayStartSlots(table, durationHours, now = new Date()) {
  const startOfDay = new Date(now)
  startOfDay.setHours(0, 0, 0, 0)
  const endOfDay = new Date(startOfDay)
  endOfDay.setDate(endOfDay.getDate() + 1)
  const reservations = table?.todayReservations || []
  const unavailableTable = !table || table.status !== 0
  const result = []

  for (let minutes = 0; minutes < 24 * 60; minutes += 30) {
    const start = new Date(startOfDay.getTime() + minutes * 60000)
    const end = new Date(start.getTime() + durationHours * 3600000)
    let reason = ''
    if (unavailableTable) reason = table?.status === 2 ? '球桌维护中' : '球桌使用中'
    else if (start <= now) reason = '该时间已过'
    else if (end > endOfDay) reason = '预约结束时间不能超过当天'
    else if (reservations.some((reservation) => overlaps(start, end, reservation))) reason = '与已有预约冲突'

    result.push({ label: timeLabel(start), value: toApiDateTime(start), disabled: Boolean(reason), reason })
  }
  return result
}
