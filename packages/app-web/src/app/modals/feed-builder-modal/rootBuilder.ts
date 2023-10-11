abstract class Builder<T> {
  constructor(private readonly parent: T) {
  }
  end() {
    return this.parent
  }
}

class SourceBuilder extends Builder<SourcesBuilder> {f
}

class SourcesBuilder extends Builder<RootBuilder> {
  private sources: SourceBuilder[] = []

  add() {
    const sourceBuilder = new SourceBuilder(this);
    this.sources.push(sourceBuilder);
    return sourceBuilder;
  }
}

class RootBuilder {
  sources = new SourcesBuilder(this)
}


const builder = new RootBuilder()
.sources.add()
