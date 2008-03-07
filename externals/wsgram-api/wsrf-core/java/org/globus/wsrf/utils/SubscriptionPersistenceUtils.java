/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.TopicListener;
import org.globus.wsrf.impl.SimpleSubscriptionTopicListener;

/**
 *
 */
public class SubscriptionPersistenceUtils
{
    /**
     * Serialize any registered subscription topic listeners of type
     * SimpleSubscriptionListener to the provided output stream.
     *
     * @param topicList The topic list on which the listeners are registered
     * @param oos       The output stream
     * @throws IOException If writing to the output stream failed
     */
    public static void storeSubscriptionListeners(
        TopicList topicList,
        ObjectOutputStream oos)
        throws IOException
    {
        Map topicListenerTable = getListenerToTopicsTable(topicList);

        oos.writeObject(topicListenerTable);
    }

    /**
     * Return a table of listeners and their corresponding topics. <p>
     * This function is used to flatten an input tree of topics into a form that
     * is simpler to persist (for instance, it is called by
     * {@link #storeSubscriptionListeners(org.globus.wsrf.TopicList,java.io.ObjectOutputStream) storeSubscriptionListeners()}).
     * <p>
     * Each key is of type
     * {@link org.globus.wsrf.impl.SimpleSubscriptionTopicListener
     * SimpleSubscriptionTopicListener}
     * and is mapped to a {@link java.util.List List} of topic paths.
     * Each topic path is a {@link java.util.List List} of
     * {@link javax.xml.namespace.QName QName} objects.
     * @param topicList TopicList The topic list on which the listeners
     *                  are registered
     * @return Map The map of listeners to topics
     */
    public static Map getListenerToTopicsTable(TopicList topicList) {
        Map topicListenerTable = new HashMap();
        Iterator topicIterator = topicList.topicIterator();
        while(topicIterator.hasNext())
        {
            addSubscriptionListenersToTable((Topic) topicIterator.next(),
                                       topicListenerTable);
        }
        return topicListenerTable;
    }

    /**
     * Gather all registered topic listeners for the topic and all of its
     * subtopics (recursive) and drop them into the provided table, which
     * maps topics to their listeners.
     *
     * @param topic              The topic to process
     * @param topicListenerTable The table into which to insert the discovered
     *                           listeners
     */
    private static void addSubscriptionListenersToTable(
        Topic topic,
        Map topicListenerTable)
    {
        Iterator topicListenerIterator = topic.topicListenerIterator();
        List topicPath = topic.getTopicPath();
        while(topicListenerIterator.hasNext())
        {
            TopicListener listener = (TopicListener)
                topicListenerIterator.next();
            if(listener instanceof SimpleSubscriptionTopicListener)
            {
                List topics;
                if((topics = (List) topicListenerTable.get(listener)) == null)
                {
                    topics = new ArrayList();
                    topics.add(topicPath);
                    topicListenerTable.put(listener, topics);
                }
                else
                {
                    topics.add(topicPath);
                }
            }
        }
        Iterator topicIterator = topic.topicIterator();
        while(topicIterator.hasNext())
        {
            addSubscriptionListenersToTable((Topic) topicIterator.next(),
                                            topicListenerTable);
        }
    }

    /**
     * Deserialize the table containing the registered subscription related
     * topic listeners and reregister said listeners in the provided topic
     * list.
     *
     * @param topicList The topic list on which to register the deserialized
     *                  listeners
     * @param ois       The input stream from which to read the table of
     *                  listeners
     * @throws IOException            If there was a IO error when reading from
     *                                the input stream
     * @throws ClassNotFoundException If the class of the object being read
     *                                could not be found
     */
    public static void loadSubscriptionListeners(
        TopicList topicList,
        ObjectInputStream ois)
        throws IOException, ClassNotFoundException
    {
        Map topicListenerTable = (Map) ois.readObject();
        Iterator topicListenerIterator =
            topicListenerTable.entrySet().iterator();
        Map.Entry tableEntry;
        TopicListener listener;
        Iterator topicIterator;
        Topic topic;

        while(topicListenerIterator.hasNext())
        {
            tableEntry = (Map.Entry) topicListenerIterator.next();
            listener = (TopicListener) tableEntry.getKey();
            topicIterator = ((List) tableEntry.getValue()).iterator();
            while(topicIterator.hasNext())
            {
                topic = topicList.getTopic((List) topicIterator.next());
                topic.addTopicListener(listener);
            }
        }
    }
}
