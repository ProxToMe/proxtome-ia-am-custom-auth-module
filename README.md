# ProxToMe IA Custom Authentication Module

## Warning
**This code is not supported by ForgeRock and it is your responsibility to verify that the software is suitable and safe for use.**

## About

The **ProxToMe IA Custom Authentication Module** (referred to below as **PICAM**) allows ForgeRock AM users to integrate
in their instance the **ProxToMe Identity Assurance Service** (referred to below as **PIAS**).

By integrating the **PICAM** on the AM Server and the provided **Mobile SDK** in mobile apps, it is possible to add
the capability of assuring proximity of the app Users to specific **ProxToMe Dongle/s** (referred to below as **Dongle/s**) as means of Authorization to access a specific **Resource**.

The **PICAM** is compatible with **AM version 5.5** and above.

The **Mobile SDK** is compatible with **iOS version 9** and above.

# Installation Instruction

## PICAM

### Installation

To install the **PICAM**, you need to have a ForgeRock AM version 5.5 instance installed on your Server.
Follow the instructions here: [AM 5.5 Installation Guide](https://backstage.forgerock.com/docs/am/5.5/install-guide/) to install AM on your Server.

Once you have your **AM** instance set up, follow this steps:

1. Download the archive at [$archive_url](https://archive.url).
2. Extract the archive on your Server.
3. Copy the **proxtome-ia-auth-module-*.jar** file to WEB-INF/lib/ where AM is deployed.
```
$ cp proxtome-ia-auth-module-*.jar /path/to/tomcat/webapps/openam/WEB-INF/lib/
```
4. Restart AM or the container in which it runs.
5. Log in to the AM console as an administrator, such as `amadmin`, and browse to `Realms > Top Level Realm > Authentication > Modules`. Click Add Module to create an instance of the **PICAM**. Name the module `ProxToMe`.
6. Click `Create`, and then configure the authentication module as appropriate. Click on `Save Changes`.

Now the **PICAM** is installed on your AM deployment.

### Configuration

1. Log in to the AM console as an administrator, such as `amadmin`, and browse to `Realms > Top Level Realm > Authentication > Chains`.
2. Click on `+ Add Chain`, then type the name of the Chain you want to use, for example `ProxToMeChain`.
3. Click on `Create`. Now click on the `+ Add a Module` button in the middle of the screen.
4. On the **Select Module** selection field, select the `ProxToMe` Module.
5. On the **Select Criteria** selection field, select `Required`.
6. Click on `OK`, then `Save Changes`.
7. Now browse to `Realms > Top Level Realm > Authorization > Policy Sets`.
8. Click on `+ New Policy Set`. Choose an **Id** and **Name** for your Policy Set, for example `ProxToMe` and `ProxToMe_Policy_Set`, and insert `URL` in the **Resource Types**.
9. Click on `Create`, then on the `+ Add a Policy` button. You should add a Policy for every URL you want to protect with the **PICAM**.
10. Insert the **Name** of the Resource you want to protect.
11. Select `URL` as **Resource Type**.
12. Insert the complete URL of the Resource you want to protect in the **Resources** field.
13. Click on `Create`. Now click on the pencil icon on the **Subjects** panel.
14. Select `Authenticated Users` in the **Type** panel. Click on **Save Changes**.
15. Click on the pencil icon on the **Actions** panel. Add all the actions (HTTP Methods) that the **PICAM** Authorization should allow the User to access. (Ex. `GET` and `POST`). Click on **Save Changes**.
16. Click on the pencil icon on the **Environments** panel. Click on **+ Add an Environment Condition**. Select `Authentication by Module Chain` as **Type** and input your Chain name as **Authenticate to Service**. In this case it would be `ProxToMeChain`.
17. Click on **Save Changes**.

Now the **PICAM** is configured, and you can test it by using the **Example App** provided with the **Mobile SDK**.

## Mobile SDK (iOS)

### Installation

To install the **Mobile SDK** in your app, you will need to follow this steps:
1. Download the archive at [$ios_archive_url](https://ios.archive.url).
2. Extract the archive on the development machine.
3. Open your app project in XCode.
4. Follow the instructions [here](https://github.com/Alamofire/Alamofire#installation) to install the **Alamofire** library in your app.
5. Open your **Target** and go in the **General** tab. Scroll to the **Embedded binaries** section.
6. Add `Alamofire.framework` and the `ProxToMeIA.framework` file included in the directory **Framework** to the **Embedded binaries** of your app.
7. In the app `Info.plist` file, add the `Privacy - Bluetooth Peripheral Usage Description` key, using as value `ProxToMe Identity Assurace`.

#### Tip
If you use **Objective C** instead of **Swift** in your project, you can follow the instructions [here](https://developer.apple.com/library/content/documentation/Swift/Conceptual/BuildingCocoaApps/MixandMatch.html) and [here](https://stackoverflow.com/questions/42693433/using-swift-framework-inside-objective-c-project) to include the `Alamofire.framework` and `ProxToMeIA.framework` in your project.

### Usage
To use the **Mobile SDK**, you have to import the `ProxToMe` class in your project.
```
import ProxToMeIA
```
You will then need to perform an Authentication process on your AM deployment to obtain a valid `authID`.
Then you have to initialize a `ProxToMe` object with an `options: [ProxToMeOptionsKey: Any]` dictionary.
The `options` dictionary shall always contain the three keys:
```
{
    ProxToMeOptionsKey.rssiTrigger: Decimal  // Contains the RSSI trigger for proximity to a Dongle. It should be in the range [0, -127]. Defaults to -62.
    ProxToMeOptionsKey.baseURL: String  // Contains the base URL of the AM Server to use.
    ProxToMeOptionsKey.authID: String  // Contains the auth ID received from AM after the authentication.
}
```
Usually, you will only need to set the `.baseURL`, `.authID` keys.
For example:
```
let options: [ProxToMeOptionsKey: Any] = [
    .rssiTrigger: ProxToMe.defaultOptions[.rssiTrigger],
    .baseURL: "<...AM Base URL...>",
    .authID: "<...AM Auth ID...>",
]
let proxtome = ProxToMe(options: options)  // ProxToMe Object initialization
```
will be a valid configuration, where:
- The `.rssiTrigger` value stays at `-62` as in the default configuration,
- The `.baseURL` value will be the URL of the example ProxToMe AM deployment,
- The `.authID` value is set with a dummy value,

You will then need to start the scan for nearby **Dongles** by calling the `start` method on the `ProxToMe` object.
```
proxtome.startScan(callback: callback(name:error:))
```
The `startScan` method takes a `callback` as parameter, that gets called when a proximity event with a **Dongle** happens.

The `callback` gets called with two parameters:
- `name: String?`, containing the nearby **Dongle** name, or `nil` if there was an error.
- `error: ProxToMeError?`, containing the error description in case of error, or `nil`.

When the `callback` method is called, you can ask the **Mobile SDK** to verify the proximity event and send it to AM to authorize access to a **Resource**.
To do this, you will need to call the method `checkProximity`.
```
proxtome.checkProximity(resourceURL: "https://my.resource.com/\(deviceId)", callback: callback(tokenId:error:))
```
The `checkProximity` method takes two parameters:
- `resourceURL: String`, containing the URL of the **Resource** to access.
- a `callback` that will be called when the proximity event is verified, and AM authorizes you to access the **Resource**.

The `callback` gets called with two parameters:
- `tokenId: String?`, containing the **tokenId** authorized by AM to access the Resource, or `nil` if there was an error.
- `error: ProxToMeError?`, containing the error description in case of error, or `nil`.

You can use the `stop` method to stop the scan for nearby **Dongles**.
```
proxtome.stop()
```

After you receive a valid `name` and `tokenId` in the `callback` function, you can use the `tokenId` to access the Resource with name `name` at the URL specified in the `options`.
To do that, you will need to set the `iPlanetDirectoryPro` header in your request to the received `tokenId`.

### Production build

The **Mobile SDK** is provided as a *Universal* framework, which means that the same `ProxToMeIA.framework` file will build both on Simulators and real iOS Devices.
Since Apple doesn’t allow the application with unused architectures to the App Store, you need to follow this steps to build your app in production with the provided **Mobile SDK**.

1. Select the Project, Choose `Target > Project Name > Select Build Phases`.
2. Press `+` and then `New Run Script Phase`.
3. Name the Script as `Remove Unused Architectures Script`.
4. This script should always be placed below the **Embed Frameworks** phase.
5. Type this in the **Custom Run Scripts**:
```
FRAMEWORK="ProxToMeIA"
FRAMEWORK_EXECUTABLE_PATH="${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}/$FRAMEWORK.framework/$FRAMEWORK"
EXTRACTED_ARCHS=()
for ARCH in $ARCHS
do
lipo -extract "$ARCH" "$FRAMEWORK_EXECUTABLE_PATH" -o "$FRAMEWORK_EXECUTABLE_PATH-$ARCH"
EXTRACTED_ARCHS+=("$FRAMEWORK_EXECUTABLE_PATH-$ARCH")
done
lipo -o "$FRAMEWORK_EXECUTABLE_PATH-merged" -create "${EXTRACTED_ARCHS[@]}"
rm "${EXTRACTED_ARCHS[@]}"
rm "$FRAMEWORK_EXECUTABLE_PATH"
mv "$FRAMEWORK_EXECUTABLE_PATH-merged" "$FRAMEWORK_EXECUTABLE_PATH"
```
This run script removes the unused architectures only while pushing the Application to the App Store.


### Example app

In the **Mobile SDK** archive, you will find an example app in the directory **Example**.
The app implements the functions described above with a demo AM deployment and an example **Resource** URL protected by it.


* * *

The contents of this file are subject to the terms of the Common Development and
Distribution License (the License). You may not use this file except in compliance with the
License.

You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
specific language governing permission and limitations under the License.

When distributing Covered Software, include this CDDL Header Notice in each file and include
the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
Header, with the fields enclosed by brackets [] replaced by your own identifying
information: "Portions copyright [year] [name of copyright owner]".

Copyright 2017 ProxToMe inc.
