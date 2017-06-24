
# react-native-gapless-sound

## Getting started

`$ npm install react-native-gapless-sound --save`

### Mostly automatic installation

`$ react-native link react-native-gapless-sound`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-gapless-sound` and add `RNGaplessSound.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNGaplessSound.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNGaplessSoundPackage;` to the imports at the top of the file
  - Add `new RNGaplessSoundPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-gapless-sound'
  	project(':react-native-gapless-sound').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-gapless-sound/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-gapless-sound')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNGaplessSound.sln` in `node_modules/react-native-gapless-sound/windows/RNGaplessSound.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Com.Reactlibrary.RNGaplessSound;` to the usings at the top of the file
  - Add `new RNGaplessSoundPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNGaplessSound from 'react-native-gapless-sound';

// TODO: What to do with the module?
RNGaplessSound;
```
  