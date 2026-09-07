import {mount} from 'svelte'
import './extensions/ArrayExtensions'
import './global.css'
import api from 'src/api/api'
import App from './App.svelte'
import {initErrorHandlers} from 'src/api/errorHandlers'
import {initSession} from 'src/stores/auth'
import type {User} from 'src/api/types'

initErrorHandlers()

const [auth] = await Promise.all([api.get<User>('user').catch(() => null)])
if (auth) initSession(auth)

mount(App, {target: document.getElementById('app')!})
document.getElementById('spinner')!.parentElement!.hidden = true
