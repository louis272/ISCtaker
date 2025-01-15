import javax.sound.sampled.{AudioSystem, Clip}

/**
 * Audio class
 * This class is used to play audio files
 *
 * @param path: Path to the audio file
 */
class Audio (path : String) {
  var audioClip: Clip = _

  try {
    val url = classOf[Audio].getResource(path)
    val audioStream = AudioSystem.getAudioInputStream(url)

    audioClip = AudioSystem.getClip
    audioClip.open(audioStream)
  } catch {
    case e: Exception =>
      e.printStackTrace()
  }

  /**
   * Play the audio
   */
  def play(): Unit = {
    // Open stream and play
    try {
      if (!audioClip.isOpen) audioClip.open()
      audioClip.stop()
      audioClip.setMicrosecondPosition(0)
      audioClip.start()
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
}