import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
} from '@angular/core';
import { GqlLogStatement } from '@feedless/graphql-api';
import { IonButton } from '@ionic/angular/standalone';
import {
  CodeEditorModalComponent,
  CodeEditorModalComponentProps,
} from '../../modals/code-editor-modal/code-editor-modal.component';
import { ModalProvider } from '../../modals/modal-provider.service';

@Component({
  selector: 'app-console-button',
  templateUrl: './console-button.component.html',
  styleUrls: ['./console-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonButton],
  standalone: true,
})
export class ConsoleButtonComponent {
  private readonly modalProvider = inject(ModalProvider);

  logs = input.required<GqlLogStatement[]>();

  openModal() {
    const props: CodeEditorModalComponentProps = {
      title: 'Log Output',
      contentType: 'text',
      readOnly: true,
      controls: false,
      text: stringifyLogStatement(this.logs()),
    };
    return this.modalProvider.openCodeEditorModal(
      CodeEditorModalComponent,
      props,
    );
  }
}

const stringifyLogStatement = (lsl: GqlLogStatement[]): string =>
  lsl
    .map((ls) => `${new Date(ls.time).toLocaleTimeString()}\t ${ls.message}`)
    .join('\n');
