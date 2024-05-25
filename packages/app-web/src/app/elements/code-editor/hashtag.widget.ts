import { Decoration, DecorationSet, EditorView, MatchDecorator, PluginValue, ViewPlugin, ViewUpdate } from '@codemirror/view';

const hashtagMatchDecoration = Decoration.mark({
  class: 'cm-hashtag',
  inclusive: true,
  tagName: 'hashtag',
});

const hashtagMatchDecorator = new MatchDecorator({
  regexp: /#([^ #]+)/g,
  decoration: hashtagMatchDecoration,
});

export const hashtagMatcher = ViewPlugin.fromClass(
  class implements PluginValue {
    decorations: DecorationSet;
    constructor(view: EditorView) {
      this.decorations = hashtagMatchDecorator.createDeco(view);
    }
    update(update: ViewUpdate) {
      this.decorations = hashtagMatchDecorator.updateDeco(
        update,
        this.decorations,
      );
    }
  },
  {
    decorations: (instance) => instance.decorations,
  },
);
