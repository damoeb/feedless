import { KeyLabelOption } from '../../components/select/select.component';
import { without } from 'lodash-es';

type OutputType = 'pixel' | 'html' | 'feed' | 'text'

class SelectBuilder {
  private _options: KeyLabelOption<OutputType>[] = [
    {
      key: 'pixel',
      label: 'Pixel',
    },
    {
      key: 'html',
      label: 'Html',
    },
    {
      key: 'text',
      label: 'Text',
    },
    {
      key: 'feed',
      label: 'Feed',
      default: true
    }
  ];

  private mimes: {[k in OutputType]: string} = {
    pixel: 'image/png',
    html: 'text/html',
    text: 'text/plain',
    feed: 'application/atom'
  }

  private picked: KeyLabelOption<OutputType>[] = [];

  pick(option: KeyLabelOption<OutputType>) {
    this.picked.push(option)
  }

  pickedOptions(): KeyLabelOption<OutputType>[] {
    return this.picked;
  }

  moreOptions(): KeyLabelOption<OutputType>[] {
    if (this.picked.length > 0) {
      if (this.picked.some(option => option.key === 'feed')) {
        return [];
      } else {
        const excluded = [
          ...this.picked.map(option => option.key),
          'feed'
        ]
        return this._options.filter(option => !excluded.includes(option.key))
      }
    }
    return this._options;
  }

  drop(option: KeyLabelOption<OutputType>) {
    this.picked = without(this.picked, option);
  }

  hasMoreOptions() {
    return this.moreOptions().length > 0
  }

  output() {
    return this.picked.map(option => option.key)
  }

  hasPicked(outputType: OutputType) {
    return this.picked.some(option => option.key === outputType);
  }

  clear() {
    this.picked = [];
  }

  isEmpty() {
    return this.picked.length === 0;
  }
}

export class ScrapeBuilder {

  select = new SelectBuilder()
}
