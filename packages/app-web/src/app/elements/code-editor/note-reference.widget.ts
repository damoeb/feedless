import { Decoration, DecorationSet, EditorView, MatchDecorator, ViewPlugin, ViewUpdate } from '@codemirror/view';

const noteReferenceMatchDecorator = new MatchDecorator({
  regexp: /(\[[^\]]+\])/g,
  boundary: /(\[[^\]]+\])/g,
  decoration: Decoration.mark({class: `cm-url`})
})

export const noteReferenceMatcher = ViewPlugin.fromClass(class {
  decorations: DecorationSet
  constructor(view: EditorView) {
    this.decorations = noteReferenceMatchDecorator.createDeco(view)
  }
  update(update: ViewUpdate) {
    this.decorations = noteReferenceMatchDecorator.updateDeco(update, this.decorations)
  }
}, {
  decorations: instance => instance.decorations,
})
