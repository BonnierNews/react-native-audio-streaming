/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react'
import {
  Platform,
  Image,
  StyleSheet,
  Text,
  View,
  FlatList,
  NativeEventEmitter,
  TouchableOpacity,
  Button,
} from 'react-native'

import { ReactNativeAudioStreaming } from 'react-native-audio-streaming'

images = {
  play: require('./icons/play.png'),
  stop: require('./icons/stop.png'),
  fastForward: require('./icons/fast_forward.png'),
  fastRewind: require('./icons/fast_rewind.png'),
  pause: require('./icons/pause.png'),
}

const reactNativeAudioStreamingEmitter = new NativeEventEmitter(
  ReactNativeAudioStreaming
)

const data = [
  {
    title: 'Studio DN special: Ulf Kristersson',
    stream: 'https://ads-e-bauerse-pods.sharp-stream.com/489/studio_dn_special_kristersson_re_dbb442cd.mp3?awCollectionId=489&awEpisodeId=36265'
  },
  {
    title: 'Studio DN special: Ebba Busch Thor',
    stream: 'https://ads-e-bauerse-pods.sharp-stream.com/489/studio_dn_special_busch_thor_rek_a61e0d33.mp3?awCollectionId=489&awEpisodeId=36250'
  },
  {
    title: 'Lasermannen dömd i Tyskland',
    stream: 'https://ads-e-bauerse-pods.sharp-stream.com/489/studiodn23feb_mixdown_0b43002f.mp3?awCollectionId=489&awEpisodeId=36238'
  },
  {
    title: 'Studio DN special: Jan Björklund',
    stream: 'https://ads-e-bauerse-pods.sharp-stream.com/489/studio_dn_special_bjorklund_rekl_8e62ce3f.mp3?awCollectionId=489&awEpisodeId=36231'
  },
  {
    title: 'Studio DN special: Annie Lööf',
    stream: 'https://ads-e-bauerse-pods.sharp-stream.com/489/studio_dn_special_annie_loof_rek_0da813c1.mp3?awCollectionId=489&awEpisodeId=36223'
  }
]

export default class App extends Component {
                 state = { status: null, progress: 0.0, duration: 0.0 }

                 componentDidMount() {
                   this.subscription = reactNativeAudioStreamingEmitter.addListener(
                     "AudioBridgeEvent",
                     event => {
                       let state = { status: event.status }
                       if (event.status === "STREAMING") {
                         state.progress = event.progress
                         state.duration = event.duration
                       }
                       this.setState(state)
                     }
                   )
                 }

                 componentWillUnmount() {
                   this.subscription.remove()
                   this.onStop()
                 }

                 renderItem = ({ item, index }) => <View style={styles.item}>
                     <Button onPress={() => this.onPlayStream(item.stream)} title={item.title} />
                     <View style={styles.spacing} />
                     {Platform.OS === "android" ? <Button onPress={() => this.onPlayStream(item.stream, 100)} title="Start at 100 seconds" /> : null}
                   </View>
                 
                 onPlayStream = (url, position = 0) => ReactNativeAudioStreaming.play(url, position)
                 
                 onPause = () => ReactNativeAudioStreaming.pause()

                 onResume = () => ReactNativeAudioStreaming.resume()

                 onStop = () => ReactNativeAudioStreaming.stop()

                 onForward = () => ReactNativeAudioStreaming.goForward(15)

                 onBack = () => ReactNativeAudioStreaming.goBack(15)

                 onPress = () => false

                 render() {
                   return <View style={styles.container}>
                       <FlatList data={data} renderItem={this.renderItem} keyExtractor={item => item.title} />
                       <View style={styles.state}>
                         <Text>Status: {this.state.status}</Text>
                         <Text>
                           Progress: {Math.round(this.state.progress)}
                         </Text>
                         <Text>
                           Duration: {Math.round(this.state.duration)}
                         </Text>
                       </View>
                       <View style={[styles.player, styles.item, styles.row]}>
                         <TouchableOpacity style={styles.iconButton} onPress={this.onBack}>
                           <Image source={images.fastRewind} />
                         </TouchableOpacity>
                         <TouchableOpacity style={styles.iconButton} onPress={this.onStop}>
                           <Image source={images.stop} />
                         </TouchableOpacity>
                         <TouchableOpacity style={styles.iconButton} onPress={this.onResume}>
                           <Image source={images.play} />
                         </TouchableOpacity>
                         <TouchableOpacity style={styles.iconButton} onPress={this.onPause}>
                           <Image source={images.pause} />
                         </TouchableOpacity>
                         <TouchableOpacity style={styles.iconButton} onPress={this.onForward}>
                           <Image source={images.fastForward} />
                         </TouchableOpacity>
                       </View>
                     </View>
                 }
               }

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
    marginVertical: 20
  },
  flatlist: {
    flex: 3
  },
  player: {
    flex: 2,
    width: '100%'
  },
  state: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: '100%',
    paddingHorizontal: 10
  },
  item: {
    margin: 5,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'center'
  },
  spacing: {
    height: 5,
  },
  iconButton: {
    width: 40,
    height: 40,
    margin: 10
  }
})
