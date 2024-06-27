;This file will be executed next to the application bundle image
;I.e. current directory will contain folder VRL-Studio with application files

; Name of the application
#define AppName "VRL-Studio"
; Version of the application
#define AppVersion "0.4.9.0"
; Menu group of the application
#define AppGroupName "VRL-Studio"
; Name of the folder that contains the application
#define AppFolderName "VRL-Studio"

[Setup]
;SignTool=signtool
AppId={{eu.mihosoft.VRL-Studio}}
AppName={#AppName}
AppVersion={#AppVersion}
AppVerName={#AppName} {#AppVersion}
AppPublisher=mihosoft (Michael Hoffer)
AppComments=VRL-Studio
AppCopyright=
AppPublisherURL=https://vrl-studio.mihosoft.eu
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
ChangesAssociations=yes
DefaultDirName={localappdata}\{#AppName}
DisableStartupPrompt=Yes
DisableDirPage=Yes
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=No
DefaultGroupName={#AppGroupName}
;Optional License
LicenseFile={#AppFolderName}\LICENSE.txt
;Win7 or above
MinVersion=6.1 
OutputBaseFilename={#AppName}-{#AppVersion}-Setup
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile={#AppFolderName}\.application\resources\mime\vrl-app-icon.ico
UninstallDisplayIcon={app}\.application\resources\mime\vrl-app-icon.ico
UninstallDisplayName={#AppName}
WizardImageStretch=Yes
WizardSmallImageFile={#AppFolderName}\.application\resources\mime\vrl-installer-img-55x55.bmp
WizardImageFile={#AppFolderName}\.application\resources\mime\vrl-installer-side-img-164x314.bmp   

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "{#AppFolderName}\VRL-Studio.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#AppFolderName}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#AppName}"; Filename: "{app}\VRL-Studio.exe"; IconFilename: "{app}\.application\resources\mime\vrl-app-icon.ico"; Check: returnTrue(); AppUserModelID: "eu.mihosoft.VRL-Studio"
Name: "{userdesktop}\{#AppName}"; Filename: "{app}\VRL-Studio.exe";  IconFilename: "{app}\.application\resources\mime\vrl-app-icon.ico"; Tasks: desktopicon; AppUserModelID: "eu.mihosoft.VRL-Studio"

[Run]
Filename: "{app}\VRL-Studio.exe"; Description: "{cm:LaunchProgram,VRL-Studio}"; Flags: nowait postinstall skipifsilent

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
Root: HKCU; Subkey: "Software\Classes\vrlstudioprojectfile\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\VRL-Studio.exe"" ""-file"" ""%1"""

[Tasks]
Name: desktopicon; Description: Create Desktop Icon.;

[UninstallDelete]
Type: filesandordirs; Name: "{localappdata}\{#AppFolderName}"