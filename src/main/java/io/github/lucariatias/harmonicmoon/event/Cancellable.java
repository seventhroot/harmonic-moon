package io.github.lucariatias.harmonicmoon.event;

public interface Cancellable {

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

}
