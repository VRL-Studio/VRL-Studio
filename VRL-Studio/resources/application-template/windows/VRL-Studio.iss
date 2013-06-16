;This file will be executed next to the application bundle image
;I.e. current directory will contain folder VRL-Studio with application files
[Setup]
AppId={{eu.mihosoft.VRL-Studio}}
AppName=VRL-Studio
AppVersion=0.4.5.5
AppVerName=VRL-Studio 0.4.5.5
AppPublisher=mihosoft (Michael Hoffer)
AppComments=VRL-Studio
AppCopyright=
AppPublisherURL=http://vrl-studio.mihosoft.eu
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
ChangesAssociations=yes
DefaultDirName={localappdata}\VRL-Studio
DisableStartupPrompt=Yes
DisableDirPage=Yes
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=VRL-Studio
;Optional License
LicenseFile=VRL-Studio\LICENSE.txt
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=VRL-Studio-0.4.5.5-Setup
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=VRL-Studio\.application\resources\mime\vrl-app-icon.ico
UninstallDisplayIcon={app}\.application\resources\mime\vrl-app-icon.ico
UninstallDisplayName=VRL-Studio
WizardImageStretch=No
;WizardSmallImageFile=VRL-Studio-setup-icon.bmp   

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "VRL-Studio\run.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "VRL-Studio\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\VRL-Studio"; Filename: "{app}\run.bat"; IconFilename: "{app}\.application\resources\mime\vrl-app-icon.ico"; Check: returnTrue()
Name: "{commondesktop}\VRL-Studio"; Filename: "{app}\run.bat";  IconFilename: "{app}\.application\resources\mime\vrl-app-icon.ico"; Check: returnFalse()

[Run]
Filename: "{app}\run.bat"; Description: "{cm:LaunchProgram,VRL-Studio}"; Flags: nowait postinstall skipifsilent

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  

[Registry]
Root: HKCU; Subkey: "Software\Classes\.vrlp"; ValueType: string; ValueName: ""; ValueData: "vrlstudioprojectfile"; Flags: uninsdeletevalue
Root: HKCU; Subkey: "Software\Classes\vrlstudioprojectfile"; ValueType: string; ValueName: ""; ValueData: "VRL-Studio Project"; Flags: uninsdeletekey
Root: HKCU; Subkey: "Software\Classes\vrlstudioprojectfile\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\.application\resources\mime\vrl-file-icon.ico,0"
Root: HKCU; Subkey: "Software\Classes\vrlstudioprojectfile\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\run.bat"" ""-file"" ""%1"""
