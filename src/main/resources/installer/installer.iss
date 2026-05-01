[Setup]
AppName=MonitoringAgentApplication
AppVersion=1.0.0
DefaultDirName={autopf}\MonitoringAgentApplication
OutputDir=.\output
OutputBaseFilename=MonitoringAgentApplication-Setup-v1.0.0
PrivilegesRequired=admin
ArchitecturesInstallIn64BitMode=x64
ArchitecturesAllowed=x64
DisableProgramGroupPage=yes
CloseApplications=no
RestartApplications=no

[Code]
var
  ConfigPage: TInputQueryWizardPage;
  OrgVal, KafkaServerVal, KafkaTopicVal, IntervalVal: string;

function GetOrg(Param: String): String; begin Result := OrgVal; end;
function GetKafkaServer(Param: String): String; begin Result := KafkaServerVal; end;
function GetKafkaTopic(Param: String): String; begin Result := KafkaTopicVal; end;
function GetInterval(Param: String): String; begin Result := IntervalVal; end;

function GetServiceParams(Param: String): String;
var
  AppPath: String;
begin
  AppPath := ExpandConstant('{app}');
  
  Result := '"-DORGANIZATION=' + OrgVal + '" ' +
            '"-DKAFKA_BOOTSTRAP_SERVERS=' + KafkaServerVal + '" ' +
            '"-DKAFKA_TOPIC=' + KafkaTopicVal + '" ' +
            '"-DCOLLECTION_INTERVAL_MS=' + IntervalVal + '" ' +
            '-jar "' + AppPath + '\monitoring-agent-1.0.0.jar"';
end;

procedure InitializeWizard();
begin
  ConfigPage := CreateInputQueryPage(wpWelcome,
    'Настройки подключения',
    'Введите параметры для работы агента',
    'Значения будут переданы сервису при запуске');
  ConfigPage.Add('Организация:', False);
  ConfigPage.Add('Сервер кафка (уточните у оператора):', False);
  ConfigPage.Add('Топик кафка (уточните у оператора):', False);
  ConfigPage.Add('Интервал сбора метрик в мс:', False);
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  if CurPageID = ConfigPage.ID then
  begin
    OrgVal := ConfigPage.Values[0];
    KafkaServerVal := ConfigPage.Values[1];
    KafkaTopicVal := ConfigPage.Values[2];
    IntervalVal := ConfigPage.Values[3];
  end;
  Result := True;
end;

[Files]
Source: "monitoring-agent-1.0.0.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "nssm.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs ignoreversion

[Run]
Filename: "{app}\nssm.exe"; Parameters: "install MonitoringAgentApp ""{app}\jre\bin\java.exe"""; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "set MonitoringAgentApp AppDirectory ""{app}"""; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "set MonitoringAgentApp AppParameters ""-DORGANIZATION={code:GetOrg} -DKAFKA_BOOTSTRAP_SERVERS={code:GetKafkaServer} -DKAFKA_TOPIC={code:GetKafkaTopic} -DCOLLECTION_INTERVAL_MS={code:GetInterval} -jar """"{app}\monitoring-agent-1.0.0.jar"""""""; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "set MonitoringAgentApp Start SERVICE_AUTO_START"; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "set MonitoringAgentApp AppRestartDelay 5000"; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "start MonitoringAgentApp"; Flags: runhidden

[UninstallRun]
Filename: "{app}\nssm.exe"; Parameters: "stop MonitoringAgentApp"; Flags: runhidden
Filename: "{app}\nssm.exe"; Parameters: "remove MonitoringAgentApp confirm"; Flags: runhidden