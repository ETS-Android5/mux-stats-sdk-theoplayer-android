package com.mux.stats.sdk.muxstats.automatedtests;

import static org.junit.Assert.fail;

import com.mux.stats.sdk.core.events.playback.PauseEvent;
import com.mux.stats.sdk.core.events.playback.PlayEvent;
import com.mux.stats.sdk.core.events.playback.PlayingEvent;
import com.mux.stats.sdk.core.events.playback.RebufferEndEvent;
import com.mux.stats.sdk.core.events.playback.RebufferStartEvent;
import com.mux.stats.sdk.core.events.playback.SeekedEvent;
import com.mux.stats.sdk.core.events.playback.SeekingEvent;

public class SeekingTestBase extends TestBase {

  protected void testSeekingWhilePaused() {
    try {
      if (!testActivity.waitForPlaybackToStart(waitForPlaybackToStartInMS)) {
        fail("Playback did not start in " + waitForPlaybackToStartInMS + " milliseconds !!!");
      }
      // play x seconds, stage 1
      Thread.sleep(PLAY_PERIOD_IN_MS);
      pausePlayer();
      Thread.sleep(PAUSE_PERIOD_IN_MS);
      // Seek to the end by triggering touch event
      testActivity.runOnUiThread(() -> {
        double duration = pView.getPlayer().getDuration();
        pView.getPlayer().setCurrentTime(duration - PLAY_PERIOD_IN_MS/1000.0);
      });
      Thread.sleep(PLAY_PERIOD_IN_MS);
      finishActivity();
      // Expected events play, playing, pause, seeking, seeked
      int playIndex = networkRequest.getIndexForFirstEvent(PlayEvent.TYPE);
      int playingIndex = networkRequest.getIndexForFirstEvent(PlayingEvent.TYPE);
      int pauseIndex = networkRequest.getIndexForFirstEvent(PauseEvent.TYPE);
      int seekingIndex = networkRequest.getIndexForFirstEvent(SeekingEvent.TYPE);
      int seekedIndex = networkRequest.getIndexForFirstEvent(SeekedEvent.TYPE);
      if (seekingIndex < playingIndex) {
        // Sometimes seeking and seeked are displayed at begining
        seekingIndex = networkRequest.getIndexForNextEvent(seekedIndex, SeekingEvent.TYPE);
        seekedIndex = networkRequest.getIndexForNextEvent(seekingIndex, SeekedEvent.TYPE);
      }
      if (!(playIndex < playingIndex
          && playingIndex < pauseIndex
          && pauseIndex < seekingIndex
          && seekingIndex < seekedIndex)) {
        fail("Bad event order: playIndex: " + playIndex + ", playingIndex: " + playingIndex
            + ", pauseIndex: " + pauseIndex + ", seekingIndex: " + seekingIndex
            + ", seekedIndex: " + seekedIndex);
      }
      playIndex = networkRequest.getIndexForNextEvent(seekedIndex - 1, PlayEvent.TYPE);
      playingIndex = networkRequest.getIndexForNextEvent(seekedIndex - 1, PlayingEvent.TYPE);
      pauseIndex = networkRequest.getIndexForNextEvent(seekedIndex - 1, PauseEvent.TYPE);
      int rebufferStartIndex = networkRequest.getIndexForNextEvent(
          seekedIndex - 1, RebufferStartEvent.TYPE);
      int rebufferEndIndex = networkRequest.getIndexForNextEvent(
          seekedIndex - 1, RebufferEndEvent.TYPE);
      if (playIndex != -1 || playingIndex != -1 || pauseIndex != -1
          || rebufferStartIndex != -1 || rebufferEndIndex != -1) {
        fail("Seeked event should be last event, found: playIndex: " + playIndex
            + ", playingIndex: " + playingIndex + ", pauseIndex: " + pauseIndex
            + ", rebufferStartIndex: " + rebufferStartIndex
            + ", rebufferEndIndex: " + rebufferEndIndex);
      }
    } catch (Exception e) {
      fail(getExceptionFullTraceAndMessage(e));
    }
  }

  protected void testSeekingWhilePlaying() {
    try {
      if (!testActivity.waitForPlaybackToStart(waitForPlaybackToStartInMS)) {
        fail("Playback did not start in " + waitForPlaybackToStartInMS + " milliseconds !!!");
      }
      // play x seconds, stage 1
      Thread.sleep(PLAY_PERIOD_IN_MS);
      // Seek to the end by triggering touch event
      testActivity.runOnUiThread(() -> {
        double duration = pView.getPlayer().getDuration();
        pView.getPlayer().setCurrentTime(duration - PLAY_PERIOD_IN_MS/1000.0);
      });
      Thread.sleep(PLAY_PERIOD_IN_MS);
      finishActivity();
      // Expected events play, playing, pause, seeking, seeked
      int playIndex = networkRequest.getIndexForFirstEvent(PlayEvent.TYPE);
      int playingIndex = networkRequest.getIndexForFirstEvent(PlayingEvent.TYPE);
      int seekingIndex = networkRequest.getIndexForNextEvent(playingIndex, SeekingEvent.TYPE);
      int seekedIndex = networkRequest.getIndexForNextEvent(playingIndex, SeekedEvent.TYPE);
      int pauseIndex = networkRequest.getIndexForNextEvent(playingIndex , PauseEvent.TYPE);
      if (!(playIndex < playingIndex
          && playingIndex < pauseIndex
          && pauseIndex < seekingIndex
          && seekingIndex < seekedIndex)) {
        fail("Bad event order: playIndex: " + playIndex + ", playingIndex: " + playingIndex
            + ", pauseIndex: " + pauseIndex + ", seekingIndex: " + seekingIndex
            + ", seekedIndex: " + seekedIndex);
      }
      playIndex = networkRequest.getIndexForNextEvent(seekedIndex - 1, PlayEvent.TYPE);
      playingIndex = networkRequest.getIndexForNextEvent(seekedIndex - 1, PlayingEvent.TYPE);
      pauseIndex = networkRequest.getIndexForNextEvent(seekedIndex - 1, PauseEvent.TYPE);
      int rebufferStartIndex = networkRequest.getIndexForNextEvent(
          seekedIndex - 1, RebufferStartEvent.TYPE);
      int rebufferEndIndex = networkRequest.getIndexForNextEvent(
          seekedIndex - 1, RebufferEndEvent.TYPE);
      if (playIndex != -1 || pauseIndex != -1 || rebufferStartIndex != -1
          || rebufferEndIndex != -1) {
        fail("Found unwanted events after seeked event: " + seekedIndex
            + ", playIndex: " + playIndex
            + ", rebufferStartIndex: " + rebufferStartIndex
            + ", pauseIndex: " + pauseIndex
            + ", rebufferEndIndex: " + rebufferEndIndex);
      }
      if (seekedIndex > playingIndex) {
        fail("Missing playing  event after seeked event: SeekedEvent: " + seekedIndex
            + ", playingIndex: " + playingIndex);
      }
    } catch (Exception e) {
      fail(getExceptionFullTraceAndMessage(e));
    }
  }
}
