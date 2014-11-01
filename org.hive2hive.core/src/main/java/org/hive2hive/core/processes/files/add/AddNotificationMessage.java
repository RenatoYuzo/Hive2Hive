package org.hive2hive.core.processes.files.add;

import java.nio.file.Path;
import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileAddEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This message is sent after an upload has finished.
 * 
 * @author Nico, Seppi
 */
public class AddNotificationMessage extends BaseDirectMessage implements IFileEventGenerator{

	private static final long serialVersionUID = -695268345354561544L;

	private static final Logger logger = LoggerFactory.getLogger(AddNotificationMessage.class);

	private final PublicKey addedFileKey;

	public AddNotificationMessage(PeerAddress targetAddress, PublicKey addedFileKey) {
		super(targetAddress);
		this.addedFileKey = addedFileKey;
	}

	@Override
	public void run() {
		logger.debug("Upload file notification message received.");

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("No user seems to be logged in.");
			return;
		}

		UserProfileManager profileManager = session.getProfileManager();

		UserProfile userProfile;
		try {
			userProfile = profileManager.getUserProfile(getMessageID(), false);
		} catch (GetFailedException e) {
			logger.error("Couldn't load user profile.", e);
			return;
		}

		Index addedFileIndex = userProfile.getFileById(addedFileKey);

		// trigger event
		Path addedFilePath = FileUtil.getPath(session.getRoot(), addedFileIndex);
		getEventBus().publish(new FileAddEvent(addedFilePath, addedFileIndex.isFile()));
	}

}