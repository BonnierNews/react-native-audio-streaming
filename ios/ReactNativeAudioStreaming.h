// AudioManager.h
// From https://github.com/jhabdas/lumpen-radio/blob/master/iOS/Classes/AudioManager.h

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import "STKAudioPlayer.h"

@interface ReactNativeAudioStreaming : RCTEventEmitter <RCTBridgeModule, STKAudioPlayerDelegate>

@property (nonatomic, strong) STKAudioPlayer *audioPlayer;
@property (nonatomic, readwrite) BOOL isPlayingWithOthers;
@property (nonatomic, readwrite) NSString *lastUrlString;
@property (nonatomic, retain) NSString *currentSong;

- (void)play:(NSString *) streamUrl;
- (void)pause;

@end
