import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
} from '@angular/core';
import { GqlLogStatement } from '../../../generated/graphql';
import { ModalService } from '../../services/modal.service';
import { CodeEditorModalComponentProps } from '../../modals/code-editor-modal/code-editor-modal.component';
import { IonButton } from '@ionic/angular/standalone';
import { CodeEditorModalModule } from '../../modals/code-editor-modal/code-editor-modal.module';

@Component({
  selector: 'app-console-button',
  templateUrl: './console-button.component.html',
  styleUrls: ['./console-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonButton, CodeEditorModalModule],
  standalone: true,
})
export class ConsoleButtonComponent {
  private readonly modalService = inject(ModalService);

  logs = input.required<GqlLogStatement[]>();

  openModal() {
    const props: CodeEditorModalComponentProps = {
      title: 'Log Output',
      contentType: 'text',
      readOnly: true,
      controls: false,
      text: stringifyLogStatement(this.logs()),
    };
    return this.modalService.openCodeEditorModal(props);
  }
}

const stringifyLogStatement = (lsl: GqlLogStatement[]): string =>
  lsl
    .map((ls) => `${new Date(ls.time).toLocaleTimeString()}\t ${ls.message}`)
    .join('\n');
