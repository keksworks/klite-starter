import {fireEvent, render} from '@testing-library/svelte'
import SortableTable from './SortableTable.svelte'
import enDict from 'i18n/en.json'

describe('SortableTable', () => {
  it('sorts items by clicking on headers', async () => {
    const items = [{a: 2, b: 'zzz'}, {a: 1, b: 'aaa'}]
    const labels = {name: 'Name', species: 'Species'}
    const columns: any[] = [['name', 'a'], ['species', 'b']]
    const {container} = render(SortableTable, {labels, items, columns, class: 'another-class'})
    expect(container.querySelector('.another-class')).to.be.ok
    expect(container.textContent).to.contain('Name')
    expect(container.textContent).to.contain('Species')

    const headers = container.querySelectorAll('th')
    await fireEvent.click(headers[0])
    expect(headers[0].classList.contains('sorted')).to.be.true
    expect(headers[0].classList.contains('asc')).to.be.true

    await fireEvent.click(headers[0])
    expect(headers[0].classList.contains('desc')).to.be.true
  })

  it('do not sort columns with empty headers', async () => {
    const items = [{a: 2, b: 'zzz'}, {a: 1, b: 'aaa'}]
    const labels = {time: 'Time'}
    const columns: any[] = [['', 'a'], ['time', 'b']]
    const {container} = render(SortableTable, {labels, items, columns, class: 'another-class'})

    const headers = container.querySelectorAll('th')
    await fireEvent.click(headers[0])
    expect(headers[0].classList.contains('sorted')).to.be.false
  })

  it('shows spinner if items not yet loaded', async () => {
    const {container} = render(SortableTable, {items: undefined, columns: []})
    expect(container.querySelector('tbody')).to.exist
    expect(container.querySelector('.spinner')).to.exist
  })

  it('handles no items', async () => {
    const {container} = render(SortableTable, {items: [], columns: []})
    expect(container.querySelector('tbody')!.textContent).to.contain(enDict.general.noItems)
  })
})
