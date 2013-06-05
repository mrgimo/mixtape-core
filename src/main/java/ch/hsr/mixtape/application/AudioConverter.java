package ch.hsr.mixtape.application;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.AudioInfo;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import it.sauronsoftware.jave.InputFormatException;
import it.sauronsoftware.jave.MultimediaInfo;

import java.io.File;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for converting all audio formats to the same output
 * format to be put on the stream.
 * 
 * @author Stefan Derungs
 */
public class AudioConverter {

	static final Logger LOG = LoggerFactory.getLogger(AudioConverter.class);

	public static enum VALID_FILE_EXTENSIONS {
		AAC, MP3, M4A, WAV
	};

	private Encoder encoder;

	public AudioConverter() {
		encoder = new Encoder();
	}

	private String mapFormatToCodec(String format) {
		switch (format) {
		case "aac":
			return "libfaad";
		case "mp3":
			return "mp3";
		case "ogg":
			return "vorbis";
		default:
			return null;
		}
	}

	/**
	 * This is just a convenience method to make sure the provided filename is a
	 * valid for audio.
	 * 
	 * @throws EncoderException
	 *             If a problem occurs calling the underlying ffmpeg executable.
	 */
	private boolean hasSupportedFileExtension(File file)
			throws EncoderException {
		final String name = file.getName().toLowerCase();
		final String extension = name.substring(name.lastIndexOf(".") + 1);
		for (String format : encoder.getSupportedEncodingFormats())
			if (format.equals(extension))
				return true;
		return false;
	}

	/**
	 * @throws EncoderException
	 *             If a problem occurs calling the underlying ffmpeg executable.
	 */
	private boolean isValidAudioFile(File file) throws EncoderException {
		return file != null && file.isFile() && hasSupportedFileExtension(file);
	}

	/**
	 * @return True if the format is supported. False else.
	 * @throws EncoderException
	 *             If a problem occurs calling the underlying ffmpeg executable.
	 */
	public boolean isSupportedDecodingFormat(String format)
			throws EncoderException {
		for (String s : encoder.getSupportedDecodingFormats())
			if (s.equals(format))
				return true;
		return false;
	}

	/**
	 * @return True if the format is supported. False else.
	 * @throws EncoderException
	 *             If a problem occurs calling the underlying ffmpeg executable.
	 */
	public boolean isSupportedEncodingFormat(String format)
			throws EncoderException {
		for (String s : encoder.getSupportedEncodingFormats())
			if (s.equals(format))
				return true;
		return false;
	}

	/**
	 * @return True if the codec is supported. False else.
	 * @throws EncoderException
	 *             If a problem occurs calling the underlying ffmpeg executable.
	 */
	public boolean isSupportedDecoder(String codec) throws EncoderException {
		for (String s : encoder.getAudioDecoders())
			if (s.equals(codec))
				return true;
		return false;
	}

	/**
	 * @return True if the codec is supported. False else.
	 * @throws EncoderException
	 *             If a problem occurs calling the underlying ffmpeg executable.
	 */
	public boolean isSupportedEncoder(String codec) throws EncoderException {
		for (String s : encoder.getAudioEncoders())
			if (s.equals(codec))
				return true;
		return false;
	}

	/**
	 * @throws UnsupportedAudioFileException
	 *             If the format of the file cannot be recognized and decoded.
	 * @throws EncoderException
	 *             If a problem occurs calling the underlying ffmpeg executable.
	 */
	public MultimediaInfo getAudioFileInfo(File file)
			throws UnsupportedAudioFileException, EncoderException {
		try {
			MultimediaInfo info = encoder.getInfo(file);
			AudioInfo audio = info.getAudio();

			if (info.getFormat().isEmpty() || audio.getDecoder().isEmpty())
				throw new UnsupportedAudioFileException(
						"The source format is not supported.");

			return info;
		} catch (InputFormatException e) {
			throw new UnsupportedAudioFileException(
					"The source format is not supported.");
		}
	}

	/**
	 * 
	 * @param source
	 *            The encoding input.
	 * @param target
	 *            The encoding output.
	 * @param targetFormat
	 *            The target file format (e.g. aac, aiff, mp3, ogg, wav, etc.)
	 * @param channels
	 *            If a number greater than 0 is provided (i.e. 1 or 2), this
	 *            will be forced, otherwise the number of channels from the
	 *            input file will be taken.
	 * @throws UnsupportedAudioFileException
	 *             If the format of the file cannot be recognized and decoded.
	 * @throws EncoderException
	 *             If a problem occurs calling the underlying ffmpeg executable.
	 */
	public void transcode(File source, File target, String targetFormat,
			int channels) throws UnsupportedAudioFileException,
			EncoderException {
		if (!isValidAudioFile(source))
			throw new IllegalArgumentException(
					"The provided source file is not a valid audio file.");

		if (target == null && !hasSupportedFileExtension(target))
			throw new IllegalArgumentException(
					"The provided target file is not a valid audio file.");

		LOG.debug("Starting transcoding of file: " + source.getName());
		MultimediaInfo sourceInfo = getAudioFileInfo(source);
		String targetCodec = mapFormatToCodec(targetFormat);
		if (!isSupportedEncodingFormat(targetFormat) || targetCodec == null
				|| !isSupportedEncoder(targetCodec))
			throw new UnsupportedAudioFileException("The target format `"
					+ targetFormat + "` is not supported.");

		EncodingAttributes encodingAttributes = new EncodingAttributes();
		AudioAttributes audioAttributes = new AudioAttributes();
		if (channels == 1 || channels == 2)
			audioAttributes.setChannels(channels);
		else if (sourceInfo.getAudio().getChannels() > 0)
			audioAttributes.setChannels(sourceInfo.getAudio().getChannels());
		audioAttributes.setCodec(targetCodec);
		audioAttributes.setBitRate(sourceInfo.getAudio().getBitRate());
		audioAttributes
				.setSamplingRate(sourceInfo.getAudio().getSamplingRate());
		encodingAttributes.setAudioAttributes(audioAttributes);
		encodingAttributes.setFormat(targetFormat);

		encoder.encode(source, target, encodingAttributes);
		LOG.debug("Transcoding finished for file: " + source.getName());
	}

}