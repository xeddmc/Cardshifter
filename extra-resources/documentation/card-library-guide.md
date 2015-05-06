**Cardshifter modding documentation**

---

#Card Library Guide

Creating and modifying the available cards is the quickest and easiest way to customize your game modification _[mod]_. This guide will explain in a detailed manner what are the different attributes, properties and effects, and how to use them effectively. 

The card library uses JavaScript Objects to store card information. This is not to be confused with typical JSON, which uses quotation marks for all field identifiers and values. JavaScript Objects only use quotation marks for String data, e.g., `"Hello, Cardshifter!"`. All identifiers, as well as numeric and boolean (`true/false`) values should be written with no quotation marks.

---

##General attributes

####`name`

- **Required**
- The unique identifier of a specific card. You must not have multiple cards with the same name, otherwise the game behavior could be unpredictable. 

Usage:

    {
        name: "My new card",
    },
    
####`flavor`

- Optional
- Flavor text that says something interesting about the card. 
 
Usage:

    {
        name: "My new card",
        flavor: "This is my favorite card of all.",
    },
    
####`creature`

- Any present value declares that the card is a creature. This means that the card can be played onto the battlefield, and will be destroyed into the discard pile upon becoming `health <= 0` or otherwise being destroyed by another mechanic.
- It also displays the creature type of the card.
- For reference, the most common creature types are:
- - `"Bio"`: Creature which can have Enchantments played on it. See Enchantments section below for details. 
- - `"Mech"`: Creature which can _not_ have Enchantments played on it. Mechs often have the characteristic that they can be sacrificed for scrap resource. See related `scrap` section. This property requires that `scrap > 0` in order to take effect.

Usage:

    {
        name: "My new card",
        flavor: "This is my favorite card of all.",
        creature: "Bio",
    },

---

##Common properties

####`health`

A creature card with `n` health can take `n` damage while it is in battle, and it is destroyed into the discard pile if the health reaches `0` or less. If you set `health: 0`, your card should die immediately after being played, save some effect(s) modify health. Usage: `health: n`

_Note: by default setting, Cardshifter cards will gain their health back after each turn._

####`attack`

A creature card with `n` attack can cause `n` damage  to a target card or player while it is in battle. Usage: `attack: n`

####`scrap`

A creature card with `n` scrap value can be sacrificed from battle after exhausting its casting `sickness`, in exchange for `n` scrap resource to the player. All cards with `scrap > 0` can be sacrificed. `scrap` resource can be used for various things which have a `scrapCost`, including most Enchantments. See the Enchantments section for details. Usage: `scrap: n`

####`sickness`

A creature card with `n` sickness must wait `n` turns after being played before it can perform an action. A creature with `0` sickness is often referenced as having "Rush" or immediate action. Usage: `sickness: n`

####`manaCost`

A card with `n` manaCost requires spending `n` player mana points in order to play it. Usage: `manaCost: n`

####`scrapCost`

A card with `n` scrapCost requires spending `n` player scrap points in order to play it. Usage: `scrapCost: n`

####`noAttack`

Declares that a creature card cannot attack. Usage: `noAttack: 1` **or** `noAttack: true`

####`denyCounterAttack`

A card with `denyCounterAttack: 1` will not be subject to the counter-attack mechanic. By default, the counter-attack mechanic makes it so that if a creature attacks another creature, and the target creature has `n` attack, the attacking creature will take `n` damage as a consequence. Setting this to `1` or `true` will override this mechanic. Usage: `denyCounterAttack: 1` **or** `denyCounterAttack: true`

---

##Enchantments

Enchantments are cards that can be cast upon cards which are in play. They modify the target creature card's properties.

####`enchantment`

When set to `true`, the card is viewed as an enchantment. Note that a card cannot be both a Creature and an Enchantment. Usage: `enchantment: true`

####`addHealth`

Add `n` health to the target card. Usage: `addHealth: n`

####`addAttack`

Add `n` attack to the target card. Usage: `addAttack: n`

_Note: More advanced Enchantment effects are described below in **Card effects**._

---

##Card effects

Card effects give special behavior to a card. Various elements can be combined flexibly in order to make cards behave in very specific ways. Please read this section very carefully before attempting to make new card effects, as the syntax is very particular. 

###Triggers

Various triggers are available for actions to be applied on. 

####`whilePresent`

Applies the contained effects while the card is present in battle. 

Usage:

    {
        name: "my card",
        whilePresent {
            // do something
        },
    },
    
####`onEndOfTurn`

Applies the contained effects at the end of a player's turn. 

Usage:

    {
        name: "my card",
        onEndOfTurn {
            // do something
        },
    },
    
####`afterPlay`

Applies the contained effects after a card is played.

Usage:

    {
        name: "my card",
        afterPlay {
            // do something
        },
    },
    
###Targets / Filters

These are used to filter the effect to a particular set of targets. 

####`"owner"`

Applies to the owner of the affected cards.

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "owner",
        },
    },
    
####`"opponent"`

Applies to the opponent of the owner of the affected cards.

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "opponent",
        },
    },
    
####`"next"`

Applies to the next player.

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "next",
        },
    },

####`"none"`

Applies to cards with no owner. 

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "none",
        },
    },
    
####`"active"`

Applies to active owner. 

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "active",
        },
    },

####`"inactive"`

Applies to inactive owner. 

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "inactive",
        },
    },

###Actions

These use the same keywords as the Common Properties.

####`set`

Set some amount of a specific resource to targets. This property can only be set on Enchantments.

Example usages:

#####Set `denyCounterAttack`

    {
        name: "my card",
        //some trigger: {
            set: {
                target: "owner",
                denyCounterAttack: 1, // or denyCounterAttack: true
            },
        },
    },
    
#####Set `attack: 1`

    {
        name: "my card",
        //some trigger: {
            set: {
                target: "owner",
                attack: 1,
            },
        },
    },
    
#####Set `health: 1`

    {
        name: "my card",
        //some trigger: {
            set: {
                target: "owner",
                health: 1,
            },
        },
    },
    