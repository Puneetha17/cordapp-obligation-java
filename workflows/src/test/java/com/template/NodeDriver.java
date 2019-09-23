package com.template;

import com.google.common.collect.ImmutableList;
import com.template.flows.IssueObligation;
import net.corda.core.contracts.Amount;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.driver.DriverParameters;
import net.corda.testing.driver.NodeHandle;
import net.corda.testing.driver.NodeParameters;
import net.corda.testing.node.TestCordapp;
import net.corda.testing.node.User;
import com.google.common.collect.ImmutableSet;

import java.util.List;

import static net.corda.finance.Currencies.POUNDS;
import static net.corda.testing.driver.Driver.driver;

/**
 * Allows you to run your nodes through an IDE (as opposed to using deployNodes). Do not use in a production
 * environment.
 */
public class NodeDriver {
    public static void main(String[] args) {
        final List<User> rpcUsers =
                ImmutableList.of(new User("user1", "test", ImmutableSet.of("ALL")));

        driver(new DriverParameters()
            .withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("net.corda.finance.contracts.asset"),
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows"),
                TestCordapp.findCordapp("net.corda.finance.schemas")))
            .withStartNodesInProcess(true).withWaitForAllNodesToFinish(true), dsl -> {
                    try {
                      CordaX500Name partyA = new CordaX500Name("PartyA", "London", "GB");
                      CordaX500Name partyB = new CordaX500Name("PartyB", "New York", "US");

                        NodeHandle nodeA = dsl.startNode(new NodeParameters()
                                .withProvidedName(partyA)
                                .withRpcUsers(rpcUsers)).get();
                        NodeHandle nodeB = dsl.startNode(new NodeParameters()
                                .withProvidedName(partyB)
                                .withRpcUsers(rpcUsers)).get();

                      Amount amount = POUNDS(1000);

                      Party lender = new Party(partyB, nodeB.getRpc().wellKnownPartyFromX500Name(partyB).getOwningKey());
                      Boolean anonymous = false;

                      SignedTransaction signedTransaction = nodeA.getRpc()
                          .startFlowDynamic(IssueObligation.Initiator.class, amount, lender, anonymous)
                          .getReturnValue()
                          .get();
                    } catch (Throwable e) {
                        System.err.println("Encountered exception in node startup: " + e.getMessage());
                        e.printStackTrace();
                    }

                    return null;
                }
        );
    }
}
