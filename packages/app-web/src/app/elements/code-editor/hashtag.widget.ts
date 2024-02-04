import {
  Decoration,
  DecorationSet,
  EditorView,
  MatchDecorator,
  ViewPlugin,
  ViewUpdate,
  WidgetType
} from "@codemirror/view";

class HashtagWidget extends WidgetType {
  constructor(private readonly tag: string) { super() }

  override toDOM(): HTMLElement {
    const wrap = document.createElement("span")
    wrap.textContent = '#'+this.tag
    wrap.className = "cm-hashtag"
    return wrap
  }

  override ignoreEvent(): boolean { return false }
}

const hashtagMatchDecorator = new MatchDecorator({
  regexp: /#([^ #]+)/g,
  boundary: /#([^ #]+)/g,
  decoration: Decoration.mark({class: `cm-hashtag`})
})

export const hashtagMatcher = ViewPlugin.fromClass(class {
  decorations: DecorationSet
  constructor(view: EditorView) {
    this.decorations = hashtagMatchDecorator.createDeco(view)
  }
  update(update: ViewUpdate) {
    this.decorations = hashtagMatchDecorator.updateDeco(update, this.decorations)
  }
}, {
  decorations: instance => instance.decorations,
  provide: plugin => EditorView.atomicRanges.of(view => {
    return view.plugin(plugin)?.decorations || Decoration.none
  })
})