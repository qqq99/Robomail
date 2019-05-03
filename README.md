SWEN30006 Software Modelling and Design  
Project 1: Robomail Revision
=============


## Team members
- Nicholas Gurban
- Andrea Law
- Qian Xu

## Assumptions
- There is only one mail room and it has only one Automail system
- An item can be carried by one to three robots
- There is at least 1 robot in the system
- Any mail items exceed the weight capacities are to be rejected
- Assume group floor is the lowest level of the building with floor value equals to 1

## How to test (one example only)
1. To test the team behaviour, open the configuration text file **automail.properties**
2. Comment out this line (add a # in front of the line):  
      Mail_Max_Weight=2000
3. Uncomment this line (remove the # in front of the line):  
      Mail_Max_Weight=3000

## Test cases
### Mail_Max_Weight
1. Mail_Max_Weight <= 2000 (need at least 1 robot for delivery)
2. Mail_Max_Weight >  2000 and <= 2600 (need at least 2 robots for delivery)
3. Mail_Max_Weight >  2600 and <= 3000 (need at least 3 robots for delivery)
4. Mail_Max_Weight >  3000 (reject mail items exceeds 3000)

### Robots
1. Robots=1
2. Robots=2
3. Robots=3
4. Robots=4 or more

### Floors
1. Any value larger than 0 (1 = the ground floor)
