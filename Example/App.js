/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  Platform,
  StyleSheet,
  Text,
  View,
  FlatList,
  NativeEventEmitter,
  TouchableOpacity,
  Button
} from 'react-native'

import { ReactNativeAudioStreaming } from 'react-native-audio-streaming'

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

type Props = {};
export default class App extends Component<Props> {
  componentDidMount () {
    reactNativeAudioStreamingEmitter.addListener(
      'AudioBridgeEvent',
      event => console.log(event)
    )
  }

  componentWillUnmount() {
    this.onStop()
  }

  renderItem = ({item}) => (
    <Button
      onPress={() => this.onPlayStream(item.stream)}
      title={item.title}
    />
  )

  onPlayStream = (url) => ReactNativeAudioStreaming.play(url, {})

  onPause = () => ReactNativeAudioStreaming.pause()

  onResume = () => ReactNativeAudioStreaming.resume()

  onStop = () => ReactNativeAudioStreaming.stop()

  onPress = () => false

  render() {
    return (
      <View style={styles.container}>
        <FlatList
          data={data}
          renderItem={this.renderItem}
        />
        <View style={styles.player}>
          <Button
            onPress={this.onResume}
            title="Resume"
          />
          <Button
            onPress={this.onPause}
            title="Pause"
          />
          <Button
            onPress={this.onStop}
            title="Stop"
          />
          <Button
            onPress={this.onPress}
            title="Skip +15"
          />
          <Button
            onPress={this.onPress}
            title="Skip -15"
          />
        </View>
      </View>
    );
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
    flex: 1
  },
  item: {
    padding: 10,
    borderBottomWidth: 0.5
  },
  itemTitle: {
    fontSize: 20
  }
});
