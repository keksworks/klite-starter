import {cleanup, fireEvent, render} from '@testing-library/svelte'
import Modal from './Modal.svelte'

afterEach(() => {
  cleanup()
})

it('Modal is shown', async () => {
  const {container, rerender} = render(Modal, {title: 'Title', show: false, flyParams: {duration: 0}})
  expect(document.body.textContent).not.to.contain('Title')

  await rerender({show: true})
  expect(document.body.textContent).to.contain('Title')
  expect(document.body.classList.contains('modal-open')).to.be.true

  await rerender({show: false})
  expect(document.body.classList.contains('modal-open')).to.be.false
})

it('body.modal-open is added on show and removed on destroy', () => {
  render(Modal, {title: 'Title', show: true, flyParams: {duration: 0}})
  expect(document.body.classList.contains('modal-open')).to.be.true
  cleanup()
  expect(document.body.classList.contains('modal-open')).to.be.false
  expect(document.querySelector('.modal')).not.to.exist
})

it('closes modal on Escape key', async () => {
  render(Modal, {title: 'Modal 1', show: true, flyParams: {duration: 0}})
  expect(document.body.textContent).to.contain('Modal 1')

  await fireEvent.keyUp(window, {code: 'Escape'})
  expect(document.body.textContent).not.to.contain('Modal 1')
  expect(document.body.classList.contains('modal-open')).to.be.false
})

it('closes only the top modal on Escape when multiple modals are open', async () => {
  render(Modal, {title: 'Modal 1', show: true, flyParams: {duration: 0}})
  render(Modal, {title: 'Modal 2', show: true, flyParams: {duration: 0}})
  render(Modal, {title: 'Modal 3', show: true, flyParams: {duration: 0}})

  expect(document.body.textContent).to.contain('Modal 1')
  expect(document.body.textContent).to.contain('Modal 2')
  expect(document.body.textContent).to.contain('Modal 3')
  expect(document.body.classList.contains('modal-open')).to.be.true

  // First Escape closes Modal 3 (top)
  await fireEvent.keyUp(window, {code: 'Escape'})
  expect(document.body.textContent).to.contain('Modal 1')
  expect(document.body.textContent).to.contain('Modal 2')
  expect(document.body.textContent).not.to.contain('Modal 3')
  expect(document.body.classList.contains('modal-open')).to.be.true

  // Second Escape closes Modal 2
  await fireEvent.keyUp(window, {code: 'Escape'})
  expect(document.body.textContent).to.contain('Modal 1')
  expect(document.body.textContent).not.to.contain('Modal 2')
  expect(document.body.textContent).not.to.contain('Modal 3')
  expect(document.body.classList.contains('modal-open')).to.be.true

  // Third Escape closes Modal 1
  await fireEvent.keyUp(window, {code: 'Escape'})
  expect(document.body.textContent).not.to.contain('Modal 1')
  expect(document.body.classList.contains('modal-open')).to.be.false
})

it('handles non-LIFO closing and reopening modals', async () => {
  const modal1 = render(Modal, {title: 'Modal 1', show: true, flyParams: {duration: 0}})
  const modal2 = render(Modal, {title: 'Modal 2', show: true, flyParams: {duration: 0}})
  const modal3 = render(Modal, {title: 'Modal 3', show: true, flyParams: {duration: 0}})

  // Close middle modal (Modal 2) programmatically
  await modal2.rerender({show: false})
  expect(document.body.textContent).to.contain('Modal 1')
  expect(document.body.textContent).not.to.contain('Modal 2')
  expect(document.body.textContent).to.contain('Modal 3')

  // Escape should close Modal 3 (top)
  await fireEvent.keyUp(window, {code: 'Escape'})
  expect(document.body.textContent).to.contain('Modal 1')
  expect(document.body.textContent).not.to.contain('Modal 3')

  // Reopen Modal 2 -> it becomes the new top modal
  await modal2.rerender({show: true})
  expect(document.body.textContent).to.contain('Modal 1')
  expect(document.body.textContent).to.contain('Modal 2')

  // Escape should close Modal 2
  await fireEvent.keyUp(window, {code: 'Escape'})
  expect(document.body.textContent).to.contain('Modal 1')
  expect(document.body.textContent).not.to.contain('Modal 2')

  // Escape should close Modal 1
  await fireEvent.keyUp(window, {code: 'Escape'})
  expect(document.body.textContent).not.to.contain('Modal 1')
  expect(document.body.classList.contains('modal-open')).to.be.false
})

it('does not close unclosable modal on Escape', async () => {
  render(Modal, {title: 'Closable Modal', show: true, flyParams: {duration: 0}})
  const unclosable = render(Modal, {title: 'Unclosable Modal', show: true, closable: false, flyParams: {duration: 0}})

  expect(document.body.textContent).to.contain('Closable Modal')
  expect(document.body.textContent).to.contain('Unclosable Modal')

  // Escape does not close unclosable modal or the one underneath
  await fireEvent.keyUp(window, {code: 'Escape'})
  expect(document.body.textContent).to.contain('Closable Modal')
  expect(document.body.textContent).to.contain('Unclosable Modal')

  // Once unclosable modal is closed programmatically, Escape closes the remaining modal
  await unclosable.rerender({show: false})
  expect(document.body.textContent).to.contain('Closable Modal')
  expect(document.body.textContent).not.to.contain('Unclosable Modal')

  await fireEvent.keyUp(window, {code: 'Escape'})
  expect(document.body.textContent).not.to.contain('Closable Modal')
  expect(document.body.classList.contains('modal-open')).to.be.false
})
