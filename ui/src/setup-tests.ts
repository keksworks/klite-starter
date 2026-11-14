import 'src/extensions/ArrayExtensions'

Element.prototype.animate = (() => ({cancel: () => {}})) as any
