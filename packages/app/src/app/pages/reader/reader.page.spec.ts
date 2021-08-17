import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReaderPage } from './reader.page';
import { ReaderPageModule } from './reader.module';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HTTP } from '@ionic-native/http/ngx';
import { TextToSpeech } from '@ionic-native/text-to-speech/ngx';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('ReaderPage', () => {
  let component: ReaderPage;
  let fixture: ComponentFixture<ReaderPage>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        ReaderPageModule,
        RouterTestingModule,
        ApolloTestingModule,
      ],
      providers: [
        { provide: HTTP, useValue: {} },
        { provide: TextToSpeech, useValue: {} },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReaderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
