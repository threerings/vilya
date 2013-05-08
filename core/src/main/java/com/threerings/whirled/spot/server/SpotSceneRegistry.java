//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/vilya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.whirled.spot.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.util.MessageManager;
import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.chat.server.SpeakUtil;
import com.threerings.crowd.data.BodyObject;

import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.ScenePlace;
import com.threerings.whirled.server.SceneRegistry;
import com.threerings.whirled.spot.client.SpotService;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotCodes;
import com.threerings.whirled.spot.data.SpotMarshaller;
import com.threerings.whirled.spot.data.SpotScene;

import static com.threerings.whirled.spot.Log.log;

/**
 * Extends the {@link SceneRegistry} with spot-related services.
 */
@Singleton
public class SpotSceneRegistry extends SceneRegistry
    implements SpotProvider
{
    /**
     * Constructs a spot scene registry.
     */
    @Inject public SpotSceneRegistry (InvocationManager invmgr)
    {
        super(invmgr);
        invmgr.registerProvider(this, SpotMarshaller.class, SceneCodes.WHIRLED_GROUP);
    }

    /**
     * Sends a cluster chat notification to the specified location in the specified place object
     * originating with the specified speaker (the speaker can be a server entity that wishes to
     * fake a "speak" message, in which case the bundle argument should be non-null and should
     * contain the id of the bundle to be used to translate the message text) and with the supplied
     * message content.
     *
     * @param sceneId the scene id in which to deliver the chat message.
     * @param speakerOid the body object id of the speaker (used to verify that they are in the
     * cluster in question).
     * @param speaker the username of the user that generated the message (or some special speaker
     * name for server messages).
     * @param bundle the bundle identifier that will be used by the client to translate the message
     * text (or null if the message originated from a real live human who wrote it in their native
     * tongue).
     * @param message the text of the chat message.
     */
    public void sendClusterChatMessage (int sceneId, int speakerOid, Name speaker,
                                        String bundle, String message, byte mode)
    {
        sendClusterChatMessage(sceneId, speakerOid,
                               new UserMessage(speaker, bundle, message, mode));
    }

    /**
     * Sends a cluster chat notification to the specified location in the specified place object
     * originating with the specified speaker.
     *
     * @param sceneId the scene id in which to deliver the chat message.
     * @param speakerOid the body object id of the speaker (used to verify that they are in the
     * cluster in question).
     * @param message the message.
     */
    public void sendClusterChatMessage (int sceneId, int speakerOid, UserMessage message)
    {
        // look up the scene manager for the specified scene
        SpotSceneManager smgr = (SpotSceneManager)getSceneManager(sceneId);
        if (smgr == null) {
            log.warning("User requested cluster chat in non-existent scene",
                "user", message.speaker, "sceneId", sceneId, "message", message);
            return;
        }

        // pass this request on to the spot scene manager
        smgr.handleClusterMessageRequest(speakerOid, message);
    }

    // from interface SpotProvider
    public void traversePortal (ClientObject caller, int sceneId, int portalId,
                                int destSceneVer, SpotService.SpotSceneMoveListener listener)
        throws InvocationException
    {
        // le sanity check
        BodyObject body = _locator.forClient(caller);
        int cSceneId = ScenePlace.getSceneId(body);
        if (cSceneId != sceneId) {
            log.info("Ignoring stale traverse portal request",
                "caller", caller.who(), "oSceneId", sceneId, "portalId", portalId,
                "cSceneId", cSceneId);
            listener.requestCancelled();
            return;
        }

        // obtain the source scene
        SpotSceneManager srcmgr = (SpotSceneManager)getSceneManager(sceneId);
        if (srcmgr == null) {
            log.warning("Traverse portal missing source scene",
                "user", body.who(), "sceneId", sceneId, "portalId", portalId);
            throw new InvocationException(SpotCodes.INTERNAL_ERROR);
        }

        // obtain the destination scene and location id
        SpotScene rss = (SpotScene)srcmgr.getScene();
        Portal dest = rss.getPortal(portalId);

        // give the source scene manager a chance to do access control
        String errmsg = srcmgr.mayTraversePortal(body, dest);
        if (errmsg != null) {
            throw new InvocationException(errmsg);
        }

        // make sure this portal has valid info
        if (dest == null || !dest.isValid()) {
            log.warning("Traverse portal with invalid portal",
                "user", body.who(), "scene", srcmgr.where(), "pid", portalId, "portal", dest,
                "portals", rss.getPortals());
            throw new InvocationException(SpotCodes.NO_SUCH_PORTAL);
        }

        // resolve their destination scene
        resolveScene(dest.targetSceneId,
                     new SpotSceneMoveHandler(_locman, srcmgr, body, destSceneVer, dest, listener));
    }

    // from interface SpotProvider
    public void changeLocation (ClientObject caller, int sceneId, Location loc,
                                SpotService.ConfirmListener listener)
        throws InvocationException
    {
        BodyObject source = _locator.forClient(caller);
        int cSceneId = ScenePlace.getSceneId(source);
        if (cSceneId != sceneId) {
            log.info("Rejecting changeLocation for invalid scene",
                "user", source.who(), "insid", cSceneId, "wantsid", sceneId, "loc", loc);
            throw new InvocationException(SpotCodes.INVALID_LOCATION);
        }

        // look up the scene manager for the specified scene
        SpotSceneManager smgr = (SpotSceneManager)getSceneManager(sceneId);
        if (smgr == null) {
            log.warning("User requested to change location from non-existent scene",
                "user", source.who(), "sceneId", sceneId, "loc", loc);
            throw new InvocationException(SpotCodes.INTERNAL_ERROR);
        }

        // pass the buck to yon scene manager
        smgr.handleChangeLoc(source, loc);

        // if that method finished, we're good to go
        listener.requestProcessed();
    }

    // from interface SpotProvider
    public void joinCluster (ClientObject caller, int friendOid,
                             SpotService.ConfirmListener listener)
        throws InvocationException
    {
        BodyObject source = _locator.forClient(caller);
        int sceneId = ScenePlace.getSceneId(source);

        // look up the scene manager for the specified scene
        SpotSceneManager smgr = (SpotSceneManager)getSceneManager(sceneId);
        if (smgr == null) {
            log.warning("User requested to join cluster from non-existent scene",
                "user", source.who(), "sceneId", sceneId, "foid", friendOid);
            throw new InvocationException(SpotCodes.INTERNAL_ERROR);
        }

        // pass the buck to yon scene manager
        smgr.handleJoinCluster(source, friendOid);

        // if that method finished, we're good to go
        listener.requestProcessed();
    }

    // from interface SpotProvider
    public void clusterSpeak (ClientObject caller, String message, byte mode)
    {
        // ensure the caller has normal chat access
        BodyObject source = _locator.forClient(caller);
        String errmsg = source.checkAccess(ChatCodes.CHAT_ACCESS, null);
        if (errmsg != null) {
            SpeakUtil.sendFeedback(source, MessageManager.GLOBAL_BUNDLE, errmsg);
        } else {
            sendClusterChatMessage(ScenePlace.getSceneId(source), source.getOid(),
                                   source.getVisibleName(), null, message, mode);
        }
    }
}
