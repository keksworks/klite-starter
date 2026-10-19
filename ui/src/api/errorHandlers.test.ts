import {t} from 'i18n'
import {handleUnhandledRejection, jsErrorHandler} from './errorHandlers'
import {get} from 'svelte/store'
import {toastStore} from 'src/stores/toasts'
import api from './api'

describe('handles unhandled promises', () => {
  let promise: Promise<any>

  function PromiseRejectionEvent(init: PromiseRejectionEventInit) {
    return Object.assign(new CustomEvent('unhandledrejection', init), init) as PromiseRejectionEvent
  }

  beforeEach(() => {
    promise = Promise.reject('')
  })

  afterEach(() => {
    toastStore.set([])
    promise.catch(() => {})
  })

  it('without translation', async () => {
    const e = PromiseRejectionEvent({reason: {message: 'no translation'}, promise})
    handleUnhandledRejection(e)
    expect(get(toastStore).last().message).to.eq('no translation')
  })

  it('with translation', async () => {
    const e = PromiseRejectionEvent({reason: {message: 'errors.technical', statusCode: 500}, promise})
    handleUnhandledRejection(e)
    expect(get(toastStore).last().message).to.eq(t.errors.technical)
  })
})

describe('jsErrorHandler', () => {
  let alert: ReturnType<typeof vi.spyOn>
  let post: ReturnType<typeof vi.spyOn>

  beforeEach(() => {
    alert = vi.spyOn(window, 'alert').mockImplementation(() => {})
    post = vi.spyOn(api, 'post').mockResolvedValue(undefined)
  })

  afterEach(() => vi.restoreAllMocks())

  it('alerts on errors from own code', () => {
    jsErrorHandler('boom', location.origin + '/assets/app.js', 1, 2)
    expect(alert).toHaveBeenCalledOnce()
    expect(alert.mock.calls[0][0]).to.contain('boom')
    expect(post).toHaveBeenCalledWith('js-error', expect.objectContaining({message: 'boom'}))
  })

  it('does not alert on errors from browser-injected scripts', () => {
    const error = new Error('SharingLinkExtractorJS.viewportDominantIFrameHandle')
    jsErrorHandler(error.message, undefined, undefined, undefined, error)
    expect(alert).not.toHaveBeenCalled()
    expect(post).toHaveBeenCalled()
  })

  it('does not alert on errors from external sources', () => {
    jsErrorHandler('boom', 'file:///System/Library/WebKit/SharingLinkExtractor.js')
    expect(alert).not.toHaveBeenCalled()
    expect(post).toHaveBeenCalled()
  })

  it('does not alert on unhandled rejections from browser-injected scripts', () => {
    const error = new Error('SharingLinkExtractorJS.viewportDominantIFrameHandle')
    const promise = Promise.reject(error)
    promise.catch(() => {})
    handleUnhandledRejection(Object.assign(new CustomEvent('unhandledrejection'), {reason: error, promise}) as PromiseRejectionEvent)
    expect(alert).not.toHaveBeenCalled()
  })
})
