import {Decoration, DecorationSet, EditorView, ViewPlugin, ViewUpdate, WidgetType} from "@codemirror/view";
import {syntaxTree} from "@codemirror/language";

class InlineImageWidget extends WidgetType {
  constructor(private readonly url: string) { super() }

  override eq(other: InlineImageWidget) { return other.url == this.url }

  override toDOM() {
    const wrap = document.createElement("div")
    wrap.setAttribute("aria-hidden", "true")
    let image = wrap.appendChild(document.createElement("img"))
    image.src = this.url
    image.className = 'cm-inline-image'
    return wrap
  }

  override ignoreEvent() { return false }
}

function inlineImages(view: EditorView) {
  let widgets: any[] = []
  for (let {from, to} of view.visibleRanges) {
    syntaxTree(view.state).iterate({
      from, to,
      enter: (node) => {
        if (node.name == "URL") {
          console.log('add this image', node.name)
          // let isTrue = view.state.doc.sliceString(node.from, node.to) == "true"
          // let deco = Decoration.widget({
          //   widget: new InlineImageWidget(isTrue),
          //   side: 1
          // })
          // widgets.push(deco.range(node.to))
        }
      }
    })
  }
  return Decoration.set(widgets)
}

function toggleBoolean(view: EditorView, pos: number) {
  let before = view.state.doc.sliceString(Math.max(0, pos - 5), pos)
  let change
  if (before == "false")
    change = {from: pos - 5, to: pos, insert: "true"}
  else if (before.endsWith("true"))
    change = {from: pos - 4, to: pos, insert: "false"}
  else
    return false
  view.dispatch({changes: change})
  return true
}


export const inlineImagePlugin = ViewPlugin.fromClass(class {
  decorations: DecorationSet

  constructor(view: EditorView) {
    this.decorations = inlineImages(view)
  }

  update(update: ViewUpdate) {
    if (update.docChanged || update.viewportChanged)
      this.decorations = inlineImages(update.view)
  }
}, {
  decorations: v => v.decorations,

  eventHandlers: {
    mousedown: (e, view) => {
      let target = e.target as HTMLElement
      if (target.nodeName == "INPUT" &&
        target.parentElement!.classList.contains("cm-boolean-toggle")) {
        return toggleBoolean(view, view.posAtDOM(target))
      }
      return false
    }
  }
})

