import {render} from '@testing-library/svelte'
import TextAreaField from './TextAreaField.svelte'

it('renders textarea with counter', () => {
  const {container} = render(TextAreaField, {label: 'notes', value: 'Hello', rows: 3})
  const textarea = container.querySelector('textarea') as HTMLTextAreaElement
  expect(textarea.id).to.eq('notes')
  expect(textarea.value).to.eq('Hello')
  expect(container.textContent).to.contain('5 / 1000')
})
