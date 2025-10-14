import {
  Decoration,
  DecorationSet,
  EditorView,
  MatchDecorator,
  PluginValue,
  ViewPlugin,
  ViewUpdate,
} from '@codemirror/view';

const urlDecoration = Decoration.mark({ class: 'cm-checkbox', inclusive: true });

const checkboxMatchDecorator = new MatchDecorator({
  regexp: /\[(x| )\]/g,
  decoration: urlDecoration,
});

export const checkboxDecorator = ViewPlugin.fromClass(
  class implements PluginValue {
    decorations: DecorationSet;

    constructor(view: EditorView) {
      this.decorations = checkboxMatchDecorator.createDeco(view);
    }

    update(update: ViewUpdate) {
      this.decorations = checkboxMatchDecorator.updateDeco(update, this.decorations);
    }
  },
  {
    decorations: (instance) => instance.decorations,
  }
);
