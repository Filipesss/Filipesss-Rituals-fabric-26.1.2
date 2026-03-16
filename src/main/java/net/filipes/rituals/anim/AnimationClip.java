package net.filipes.rituals.anim;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public final class AnimationClip {
    public enum LoopMode { ONCE, LOOP, PINGPONG }

    public final String name;
    public final float lengthSec;
    public final LoopMode loop;
    public final Map<String, BoneTrack> tracks;

    public AnimationClip(String name, float lengthSec, LoopMode loop, Collection<BoneTrack> tracks) {
        this.name = name;
        this.lengthSec = lengthSec;
        this.loop = loop;
        this.tracks = tracks.stream().collect(Collectors.toMap(BoneTrack::bone, t -> t));
    }
}
