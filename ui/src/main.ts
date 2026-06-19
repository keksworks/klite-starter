import {mount} from 'svelte'
import './extensions/ArrayExtensions'
import './global.css'
import App from './App.svelte'
import {initErrorHandlers} from 'src/api/errorHandlers'

initErrorHandlers()

mount(App, {target: document.getElementById('app')!})
document.getElementById('spinner')!.parentElement!.hidden = true
