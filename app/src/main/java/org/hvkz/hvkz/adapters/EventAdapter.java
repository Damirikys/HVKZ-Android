package org.hvkz.hvkz.adapters;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public abstract class EventAdapter implements ChildEventListener
{
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        onDataWasChanged();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        onDataWasChanged();
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        onDataWasChanged();
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        onDataWasChanged();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    public abstract void onDataWasChanged();
}
