import api from './api'
import {resolve, t} from 'i18n'
import {showToast, ToastType} from 'src/stores/toasts'
import {navigate} from '@keksworks/svelte-tiny-router'

export function jsErrorHandler(message: Event|string, source?: string, line?: number, column?: number, error?: Error) {
  reportError({message, source, line, column}, error)
  if (isExternalError(message, source, error)) return console.error(message, error)
  alert((error?.name == 'SyntaxError' ? 'Your browser is probably too old, please update:' : 'Technical error occurred, please reload the page:') + '\n' + message)
}

// browser-injected scripts (e.g. WebKit's SharingLinkExtractor behind the share sheet) throw errors we cannot fix, don't bother the user with them
function isExternalError(message: Event|string, source?: string, error?: Error) {
  if (typeof message != 'string') return true
  return /\w+JS\.\w/.test(message + (error?.stack ?? '')) || (!!source && !source.startsWith(location.origin))
}

export function reportError(body: object, error?: Error) {
  api.post('js-error', {...body,
    href: location.href,
    userAgent: navigator.userAgent,
    stack: error?.stack
  }).catch(e => console.error(e))
}

export function handleUnhandledRejection(event: PromiseRejectionEvent) {
  const e: Error & {statusCode: number} | undefined = event.reason
  console.error(e)
  if (e?.stack) return jsErrorHandler(e.message, undefined, undefined, undefined, e)
  let message = e?.message
  if (message) message = resolve(message) as string ?? message
  else message = t.errors.technical + ': ' + e
  showToast(message, {type: ToastType.ERROR})
  if (e?.statusCode === 401 || e?.statusCode === 403) setTimeout(() => navigate('/'), 1000)
}

export function initErrorHandlers() {
  window.onerror = jsErrorHandler
  window.addEventListener('unhandledrejection', handleUnhandledRejection)
  window.addEventListener('vite:preloadError', () => location.reload())
}
