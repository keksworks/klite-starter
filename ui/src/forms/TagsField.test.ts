import {fireEvent, render} from '@testing-library/svelte'
import TagsField from './TagsField.svelte'

it('adds and removes tags', async () => {
  const {container} = render(TagsField, {label: 'Emails', tags: ['a@example.com']})
  expect(container.textContent).to.contain('Emails')
  expect(container.textContent).to.contain('a@example.com')

  const input = container.querySelector('input')!
  await fireEvent.input(input, {target: {value: 'b@example.com'}})
  await fireEvent.keyDown(input, {key: 'Enter'})
  expect(container.textContent).to.contain('b@example.com')

  await fireEvent.click(container.querySelector('.chip-remove')!)
  expect(container.textContent).to.not.contain('a@example.com')
})
