import {
  Decoration,
  DecorationSet,
  EditorView,
  MatchDecorator,
  PluginValue,
  ViewPlugin,
  ViewUpdate,
} from '@codemirror/view';

const urlDecoration = Decoration.mark({ class: 'cm-url', inclusive: true });

const urlMatchDecorator = new MatchDecorator({
  regexp: /(https?:\/\/[.a-zA-Z0-9_\-]+\.[a-zA-Z0-9_\-]+[^ )\]]*)/g,
  decoration: urlDecoration,
});

export const urlDecorator = ViewPlugin.fromClass(
  class implements PluginValue {
    decorations: DecorationSet;

    constructor(view: EditorView) {
      this.decorations = urlMatchDecorator.createDeco(view);
    }

    update(update: ViewUpdate) {
      this.decorations = urlMatchDecorator.updateDeco(update, this.decorations);
    }
  },
  {
    decorations: (instance) => instance.decorations,
  }
);
