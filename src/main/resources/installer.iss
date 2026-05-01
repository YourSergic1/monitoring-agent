[Setup]
AppName=MonitoringAgentApplication
AppVersion=1.1.0
DefaultDirName={autopf}\MonitoringAgentApplication
OutputDir=.\output
OutputBaseFilename=MonitoringAgentApplication-Setup-v1.1.0
PrivilegesRequired=admin
ArchitecturesInstallIn64BitMode=x64
ArchitecturesAllowed=x64
DisableProgramGroupPage=yes
CloseApplications=no
RestartApplications=no

[Code]
var
  ConfigPage: TInputQueryWizardPage;
  OrgVal, KafkaServerVal, KafkaMetricsTopicVal, KafkaInitTopicVal, IntervalVal, StartTimeVal, EndTimeVal: string;
  ContinuousCheckbox: TNewCheckBox;
  ContinuousVal: Boolean;
  OriginalFormHeight: Integer;

function GetOrg(Param: String): String; begin Result := OrgVal; end;
function GetKafkaServer(Param: String): String; begin Result := KafkaServerVal; end;
function GetKafkaMetricsTopic(Param: String): String; begin Result := KafkaMetricsTopicVal; end;
function GetKafkaInitTopic(Param: String): String; begin Result := KafkaInitTopicVal; end;
function GetInterval(Param: String): String; begin Result := IntervalVal; end;
function GetContinuous(Param: String): String; begin if ContinuousVal then Result := 'true' else Result := 'false'; end;
function GetStartTime(Param: String): String; begin Result := StartTimeVal; end;
function GetEndTime(Param: String): String; begin Result := EndTimeVal; end;

procedure ContinuousCheckboxClick(Sender: TObject);
begin
  ContinuousVal := ContinuousCheckbox.Checked;
  ConfigPage.Edits[5].Enabled := not ContinuousVal;
  ConfigPage.Edits[6].Enabled := not ContinuousVal;
  if ContinuousVal then
  begin
    ConfigPage.PromptLabels[5].Font.Color := clGray;
    ConfigPage.PromptLabels[6].Font.Color := clGray;
  end
  else
  begin
    ConfigPage.PromptLabels[5].Font.Color := clWindowText;
    ConfigPage.PromptLabels[6].Font.Color := clWindowText;
  end;
end;

procedure InitializeWizard();
var
  Gap, CheckTop: Integer;
begin
  OriginalFormHeight := WizardForm.ClientHeight;
  ConfigPage := CreateInputQueryPage(wpWelcome, 'Настройки подключения', 'Введите параметры для работы агента', 'Значения будут переданы сервису при запуске');
  ConfigPage.Add('Организация (на английском):', False);
  ConfigPage.Add('Сервер кафка (уточните у оператора):', False);
  ConfigPage.Add('Топик кафка для метрик (уточните у оператора):', False);
  ConfigPage.Add('Топик кафка для инициализации (уточните у оператора):', False);
  ConfigPage.Add('Интервал сбора метрик в мс:', False);
  ConfigPage.Add('Начало мониторинга (HH:MM):', False);
  ConfigPage.Add('Окончание мониторинга (HH:MM):', False);

  ContinuousCheckbox := TNewCheckBox.Create(ConfigPage);
  ContinuousCheckbox.Parent := ConfigPage.Surface;
  ContinuousCheckbox.Caption := 'Круглосуточный мониторинг (игнорировать время)';
  ContinuousCheckbox.Checked := False;
  ContinuousCheckbox.OnClick := @ContinuousCheckboxClick;

  Gap := ScaleY(8);
  CheckTop := ConfigPage.Edits[6].Top + ConfigPage.Edits[6].Height + Gap;
  ContinuousCheckbox.Top := CheckTop;
  ContinuousCheckbox.Left := ConfigPage.PromptLabels[0].Left;
  ContinuousCheckbox.Width := ConfigPage.Surface.Width - ContinuousCheckbox.Left - ScaleX(10);

  ConfigPage.Surface.Height := CheckTop + ContinuousCheckbox.Height + ScaleY(10);

  ContinuousVal := ContinuousCheckbox.Checked;
  ConfigPage.Edits[5].Enabled := not ContinuousVal;
  ConfigPage.Edits[6].Enabled := not ContinuousVal;
  ConfigPage.PromptLabels[5].Font.Color := clWindowText;
  ConfigPage.PromptLabels[6].Font.Color := clWindowText;
end;

procedure CurPageChanged(CurPageID: Integer);
begin
  if CurPageID = ConfigPage.ID then
    WizardForm.ClientHeight := OriginalFormHeight + ScaleY(140)
  else
    WizardForm.ClientHeight := OriginalFormHeight;
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;
  if CurPageID = ConfigPage.ID then
  begin
    OrgVal := Trim(ConfigPage.Values[0]);
    KafkaServerVal := Trim(ConfigPage.Values[1]);
    KafkaMetricsTopicVal := Trim(ConfigPage.Values[2]);
    KafkaInitTopicVal := Trim(ConfigPage.Values[3]);
    IntervalVal := Trim(ConfigPage.Values[4]);
    ContinuousVal := ContinuousCheckbox.Checked;

    if OrgVal = '' then begin MsgBox('Укажите организацию.', mbError, MB_OK); Result := False; Exit; end;
    if KafkaServerVal = '' then begin MsgBox('Укажите сервер Kafka.', mbError, MB_OK); Result := False; Exit; end;
    if KafkaMetricsTopicVal = '' then begin MsgBox('Укажите топик для метрик.', mbError, MB_OK); Result := False; Exit; end;
    if KafkaInitTopicVal = '' then begin MsgBox('Укажите топик для инициализации.', mbError, MB_OK); Result := False; Exit; end;
    if IntervalVal = '' then begin MsgBox('Укажите интервал сбора.', mbError, MB_OK); Result := False; Exit; end;

    if not ContinuousVal then
    begin
      StartTimeVal := Trim(ConfigPage.Values[5]);
      EndTimeVal := Trim(ConfigPage.Values[6]);
      if (StartTimeVal = '') or (EndTimeVal = '') then
      begin
        MsgBox('Укажите время начала и окончания мониторинга.', mbError, MB_OK);
        Result := False;
        Exit;
      end;
    end
    else
    begin
      StartTimeVal := '00:00';
      EndTimeVal := '23:59';
    end;
  end;
end;

[Files]
Source: "monitoring-agent-1.1.0.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "nssm.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs ignoreversion

[Run]
Filename: "{app}\nssm.exe"; Parameters: "install MonitoringAgentApp ""{app}\jre\bin\java.exe"""; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "set MonitoringAgentApp AppDirectory ""{app}"""; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "set MonitoringAgentApp AppParameters ""-DORGANIZATION={code:GetOrg} -DKAFKA_BOOTSTRAP_SERVERS={code:GetKafkaServer} -DKAFKA_METRICS_TOPIC={code:GetKafkaMetricsTopic} -DKAFKA_INIT_TOPIC={code:GetKafkaInitTopic} -DCOLLECTION_INTERVAL_MS={code:GetInterval} -DCONTINUOUS={code:GetContinuous} -DSTART_TIME={code:GetStartTime} -DEND_TIME={code:GetEndTime} -jar """"{app}\monitoring-agent-1.1.0.jar"""""""; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "set MonitoringAgentApp Start SERVICE_AUTO_START"; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "set MonitoringAgentApp AppRestartDelay 5000"; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "start MonitoringAgentApp"; Flags: runhidden

[UninstallRun]
Filename: "{app}\nssm.exe"; Parameters: "stop MonitoringAgentApp"; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "remove MonitoringAgentApp confirm"; Flags: runhidden