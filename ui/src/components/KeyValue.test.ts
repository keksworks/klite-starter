import {render} from '@testing-library/svelte'
import KeyValue from './KeyValue.svelte'

it('renders label and value', () => {
  const {container} = render(KeyValue, {label: 'Name', value: 'Ringo'})
  expect(container.textContent).to.contain('Name')
  expect(container.textContent).to.contain('Ringo')
})

it('hides when value is falsy', () => {
  const {container} = render(KeyValue, {label: 'Name', value: undefined})
  expect(container.textContent).to.not.contain('Name')
})
