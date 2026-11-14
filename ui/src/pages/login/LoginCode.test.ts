import {fireEvent, render, waitFor} from '@testing-library/svelte'
import {get} from 'svelte/store'
import {t} from 'i18n'
import api from 'src/api/api'
import {toastStore} from 'src/stores/toasts'
import LoginCode from './LoginCode.svelte'

vi.mock('src/api/api', () => ({default: {post: vi.fn()}}))
vi.mock('@keksworks/svelte-tiny-router', () => ({navigate: vi.fn()}))

describe('LoginCode', () => {
  it('resets to the initial form and shows an error after too many code tries', async () => {
    vi.mocked(api.post).mockRejectedValue({message: 'login.tooManyCodeTries'})
    const {container} = render(LoginCode, {waitingForCode: true})

    await fireEvent.submit(container.querySelector('form')!)

    await waitFor(() => expect(container.querySelector('input[type=email]')).to.exist)
    expect(container.querySelector('input[inputmode=numeric]')).to.not.exist
    expect(get(toastStore).last()?.message).to.eq(t.login.tooManyCodeTries)
  })
})
