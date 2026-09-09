import 'src/extensions/ArrayExtensions'

// not provided by jsdom
window.localStorage = {} as Storage
Element.prototype.animate = (() => ({cancel: () => {}})) as any
