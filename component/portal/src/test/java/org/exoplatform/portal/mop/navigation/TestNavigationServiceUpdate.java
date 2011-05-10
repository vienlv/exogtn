/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.portal.mop.navigation;

import org.exoplatform.portal.mop.SiteKey;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.core.api.MOPService;

import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestNavigationServiceUpdate extends AbstractTestNavigationService
{

   public void testNoop() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_no_op");
      Navigation def = portal.getRootNavigation().addChild("default");
      def.addChild("a");
      def.addChild("b");
      def.addChild("c");
      def.addChild("d");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_no_op"));
      NodeContext<Node> root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null);
      Iterator<NodeChange<Node>> it = root.node.update(service, null);
      assertFalse(it.hasNext());
   }

   public void testHasChanges() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_cannot_save");
      Navigation def = portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_no_op"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();

      //
      assertFalse(root.context.hasChanges());
      root.addChild("foo");
      assertTrue(root.context.hasChanges());

      //
      try
      {
         root.update(service, null);
      }
      catch (IllegalArgumentException expected)
      {
      }

      //
      assertTrue(root.context.hasChanges());
      service.saveNode(root.context);
      assertFalse(root.context.hasChanges());

      //
      Iterator<NodeChange<Node>> it = root.update(service, null);
      assertFalse(it.hasNext());
   }

   public void testAddFirst() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_add_first");
      portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_add_first"));
      NodeContext<Node> root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null);
      assertEquals(0, root1.getNodeSize());
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      root2.addChild("a");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      root1.node.update(service, null);
      assertEquals(1, root1.getNodeSize());
      Node a = root1.getNode(0);
      assertEquals("a", a.getName());

   }

   public void testAddSecond() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_add_second");
      portal.getRootNavigation().addChild("default").addChild("a");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_add_second"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      Node a = root1.getChild("a");
      assertEquals(1, root1.getSize());
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      root2.addChild("b");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      Iterator<NodeChange<Node>> changes = root1.update(service, null);
      NodeChange.Added<Node> added = (NodeChange.Added<Node>)changes.next();
      assertSame(root1, added.parent);
      assertSame(root1.getChild("b"), added.node);
      assertSame(a, added.previous);
      assertFalse(changes.hasNext());
      assertEquals(2, root1.getSize());
      assertEquals("a", root1.getChild(0).getName());
      assertEquals("b", root1.getChild(1).getName());
   }

   public void testRemove() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_remove");
      portal.getRootNavigation().addChild("default").addChild("a");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_remove"));
      NodeContext<Node> root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null);
      assertEquals(1, root1.getNodeSize());
      Node a = root1.getNode("a");
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      root2.removeChild("a");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      Iterator<NodeChange<Node>> changes = root1.node.update(service, null);
      NodeChange.Removed<Node> removed = (NodeChange.Removed<Node>)changes.next();
      assertSame(root1.node, removed.parent);
      assertSame(a, removed.node);
      assertFalse(changes.hasNext());
      assertEquals(0, root1.getNodeSize());
   }

   public void testMove() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_move");
      portal.getRootNavigation().addChild("default").addChild("a").addChild("b");
      portal.getRootNavigation().getChild("default").addChild("c");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_move"));
      NodeContext<Node> root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null);
      assertEquals(2, root1.getNodeSize());
      Node a = root1.getNode("a");
      Node b = a.getChild("b");
      Node c = root1.getNode("c");
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      root2.getChild("c").addChild(root2.getChild("a").getChild("b"));
      service.saveNode(root2.context);

      //
      sync(true);

      //
      Iterator<NodeChange<Node>> changes = root1.node.update(service, null);
      NodeChange.Moved<Node> moved = (NodeChange.Moved<Node>)changes.next();
      assertSame(a, moved.from);
      assertSame(c, moved.to);
      assertSame(b, moved.node);
      assertSame(null, moved.previous);
      assertFalse(changes.hasNext());
      assertEquals(0, root1.getNode("a").getSize());
      assertEquals(1, root1.getNode("c").getSize());
   }

   public void testAddWithSameName() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_add_with_same_name");
      portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_add_with_same_name"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      root1.addChild("a").addChild("b");
      root1.addChild("c");
      service.saveNode(root1.context);

      //
      sync(true);

      //
      root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      Node a = root1.getChild("a");
      Node b = a.getChild("b");
      Node c = root1.getChild("c");

      //
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      root2.getChild("c").addChild(root2.getChild("a").getChild("b"));
      Node b2 = root2.getChild("a").addChild("b");
      service.saveNode(root2.context);

      //
      Iterator<NodeChange<Node>> changes = root1.update(service, null);
      NodeChange.Added<Node> added = (NodeChange.Added<Node>)changes.next();
      assertNull(added.previous);
      assertSame(a, added.parent);
      NodeChange.Moved<Node> moved = (NodeChange.Moved<Node>)changes.next();
      assertNull(moved.previous);
      assertSame(a, moved.from);
      assertSame(c, moved.to);
      assertSame(b, moved.node);
      assertFalse(changes.hasNext());

      //
      assertSame(a, root1.getChild("a"));
      assertSame(c, root1.getChild("c"));
      assertSame(b, c.getChild("b"));
      assertEquals(b2.getId(), a.getChild("b").getId());
      assertSame(a.getChild("b"), added.node);
   }

   public void testComplex() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_complex");
      portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_complex"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      Node a1 = root1.addChild("a");
      a1.addChild("c");
      a1.addChild("d");
      a1.addChild("e");
      Node b1 = root1.addChild("b");
      b1.addChild("f");
      b1.addChild("g");
      b1.addChild("h");
      service.saveNode(root1.context);

      //
      sync(true);

      //
      root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      a1 = root1.getChild("a");
      Node c1 = a1.getChild("c");
      Node d1 = a1.getChild("d");
      Node e1 = a1.getChild("e");
      b1 = root1.getChild("b");
      Node f1 = b1.getChild("f");
      Node g1 = b1.getChild("g");
      Node h1 = b1.getChild("h");

      //
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      Node a2 = root2.getChild("a");
      a2.removeChild("e");
      Node b2 = root2.getChild("b");
      b2.addChild(2, a2.getChild("d"));
      a2.addChild(1, "d");
      b2.removeChild("g");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      Iterator<NodeChange<Node>> changes = root1.update(service, null);
      NodeChange.Added<Node> added = (NodeChange.Added<Node>)changes.next();
      assertSame(a1, added.parent);
      assertEquals("d", added.node.getName());
      assertSame(c1, added.previous);
      NodeChange.Removed<Node> removed1 = (NodeChange.Removed<Node>)changes.next();
      assertSame(a1 , removed1.parent);
      assertSame(e1 , removed1.node);
      NodeChange.Moved<Node> moved = (NodeChange.Moved<Node>)changes.next();
      assertSame(a1 , moved.from);
      assertSame(b1 , moved.to);
      assertSame(d1 , moved.node);
      assertSame(f1 , moved.previous);
      NodeChange.Removed<Node> removed2 = (NodeChange.Removed<Node>)changes.next();
      assertSame(b1 , removed2.parent);
      assertSame(g1 , removed2.node);
      assertFalse(changes.hasNext());

      //
      assertSame(a1, root1.getChild("a"));
      assertSame(b1, root1.getChild("b"));
      assertEquals(2, a1.getSize());
      assertSame(c1, a1.getChild(0));
      assertNotNull(a1.getChild(1));
      assertEquals("d", a1.getChild(1).getName());
      assertFalse(d1.getId().equals(a1.getChild(1).getId()));
      assertEquals(3, b1.getSize());
      assertSame(f1, b1.getChild(0));
      assertSame(d1, b1.getChild(1));
      assertSame(h1, b1.getChild(2));
   }

   public void testReplaceChild() throws NullPointerException, NavigationServiceException
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_replace_child");
      portal.getRootNavigation().addChild("default").addChild("foo");
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_replace_child"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
      String foo1Id = root1.getChild("foo").getId();

      //
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
      root2.removeChild("foo");
      Node foo = root2.addChild("foo");
      foo.setState(new NodeState.Builder().setLabel("foo2").capture());
      service.saveNode(root2.context);
      String foo2Id = foo.getId();
      sync(true);

      //
      Iterator<NodeChange<Node>> changes = root1.update(service, null);
      NodeChange.Added<Node> added = (NodeChange.Added<Node>)changes.next();
      assertEquals(foo2Id, added.getNode().getId());
      NodeChange.Removed<Node> removed = (NodeChange.Removed<Node>)changes.next();
      assertEquals(foo1Id, removed.getNode().getId());
      assertFalse(changes.hasNext());

      //
      foo = root1.getChild("foo");
      assertEquals(foo2Id, foo.getId());
      assertEquals("foo2", root1.getChild("foo").getState().getLabel());
   }

   public void testRename() throws NullPointerException, NavigationServiceException
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_rename");
      portal.getRootNavigation().addChild("default").addChild("foo");
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_rename"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();

      //
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
      root2.getChild("foo").setName("bar");
      service.saveNode(root2.context);
      sync(true);

      //
      Iterator<NodeChange<Node>> it = root1.update(service, null);
      Node bar = root1.getChild(0);
      assertEquals("bar", bar.getName());
      NodeChange.Renamed<Node> renamed = (NodeChange.Renamed<Node>)it.next();
      assertEquals("bar", renamed.getName());
      assertSame(bar, renamed.getNode());
   }

   public void testState() throws NullPointerException, NavigationServiceException
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_state");
      portal.getRootNavigation().addChild("default").addChild("foo").addChild("bar");
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_state"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
      Node root3 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();

      //
      Node root = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
      root.getChild("foo").setState(new NodeState.Builder().setLabel("foo").capture());
      service.saveNode(root.context);
      sync(true);

      //
      Iterator<NodeChange<Node>> changes = root1.update(service, Scope.GRANDCHILDREN);
      Node foo = root1.getChild("foo");
      assertEquals("foo", foo.getState().getLabel());
      NodeChange.Updated<Node> updated = (NodeChange.Updated<Node>)changes.next();
      assertSame(foo, updated.getNode());
      assertEquals(new NodeState.Builder().setLabel("foo").capture(), updated.getState());
      NodeChange.Added<Node> added = (NodeChange.Added<Node>)changes.next();
      assertEquals("bar", added.getNode().getName());
      assertEquals(null, added.previous);
      assertEquals("bar", added.name);
      assertFalse(changes.hasNext());

      //
      changes = root2.update(service, null);
      foo = root2.getChild("foo");
      assertEquals("foo", foo.getState().getLabel());
      updated = (NodeChange.Updated<Node>)changes.next();
      assertSame(foo, updated.getNode());
      assertEquals(new NodeState.Builder().setLabel("foo").capture(), updated.getState());
      assertFalse(changes.hasNext());

      //
      changes = root3.update(service, null);
      foo = root3.getChild("foo");
      assertEquals("foo", foo.getState().getLabel());
      updated = (NodeChange.Updated<Node>)changes.next();
      assertSame(foo, updated.getNode());
      assertEquals(new NodeState.Builder().setLabel("foo").capture(), updated.getState());
      assertFalse(changes.hasNext());
   }

   public void testUseMostActualChildren() throws NullPointerException, NavigationServiceException
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_with_most_actual_children");
      portal.getRootNavigation().addChild("default").addChild("foo").addChild("bar");
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_with_most_actual_children"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
      Node foo = root.getChild("foo");
      sync(true);

      //
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      root1.getChild("foo").removeChild("bar");
      service.saveNode(root1.context);
      sync(true);

      //
      foo.update(service, Scope.CHILDREN);
      assertNull(foo.getChild("bar"));

      // Update a second time (it actually test a previous bug)
      foo.update(service, Scope.CHILDREN);
      assertNull(foo.getChild("bar"));
   }

   public void testUpdateDeletedNode() throws NullPointerException, NavigationServiceException
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_deleted_node");
      portal.getRootNavigation().addChild("default").addChild("foo").addChild("bar");
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_deleted_node"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      Node bar = root.getChild("foo").getChild("bar");
      sync(true);

      //
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
      root1.getChild("foo").removeChild("bar");
      service.saveNode(root1.context);
      sync(true);

      //
      try
      {
         bar.update(service, Scope.CHILDREN);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertSame(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE, e.getError());
      }
   }

   public void testLoadEvents() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_load_events");
      Navigation fooNav = portal.getRootNavigation().addChild("default").addChild("foo");
      fooNav.addChild("bar1");
      fooNav.addChild("bar2");
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_load_events"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.SINGLE, null).getNode();

      //
      Iterator<NodeChange<Node>> changes = root.update(service, Scope.ALL);

      //
      Node foo = root.getChild(0);
      assertEquals("foo", foo.getName());
      Node bar1 = foo.getChild(0);
      assertEquals("bar1", bar1.getName());
      Node bar2 = foo.getChild(1);
      assertEquals("bar2", bar2.getName());

      //
      NodeChange.Added<Node> added1 = (NodeChange.Added<Node>)changes.next();
      assertSame(foo, added1.getNode());
      NodeChange.Added<Node> added2 = (NodeChange.Added<Node>)changes.next();
      assertSame(bar1, added2.getNode());
      NodeChange.Added<Node> added3 = (NodeChange.Added<Node>)changes.next();
      assertSame(bar2, added3.getNode());
      assertFalse(changes.hasNext());
   }
}
