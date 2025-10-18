// File: kidcode-web/src/main/resources/static/examples.js

window.examples = {
  /* 🟢 EASY LEVEL (Basics) */

  "Say Hello": `# Cody says hello to you!
say "Hello, KidCoder!"
say "Let's create something fun today!"`,

  "Move and Turn": `# Cody moves and turns
move forward 100
turn right 90
move forward 100
turn right 90
say "I made an L shape!"`,

  "Simple Square": `# Cody draws a square
repeat 4
    move forward 100
    turn right 90
end repeat`,

  "Pen Up and Down": `# Move without drawing, then draw again
pen down
move forward 100
pen up
move forward 50
pen down
move forward 100
say "Cody lifted the pen halfway!"`,

  /* 🟡 INTERMEDIATE LEVEL (Loops & Colors) */

  "Colored Steps": `# Cody draws colored steps
set colors = ["red", "green", "blue", "purple"]
set color_index = 0

repeat 4
    color colors[color_index]
    move forward 80
    turn right 90
    move forward 40
    turn left 90
    set color_index = color_index + 1
end repeat`,

  "Triangle Pattern": `# Draws three colorful triangles
set colors = ["red", "yellow", "blue"]
set color_index = 0

repeat 3
    color colors[color_index]
    repeat 3
        move forward 100
        turn right 120
    end repeat
    turn right 120
    set color_index = color_index + 1
end repeat`,

  "Flower Petals": `# Create a simple flower shape
set colors = ["pink", "red", "orange"]
set color_index = 0

repeat 12
    color colors[color_index]
    repeat 2
        move forward 80
        turn right 60
        move forward 80
        turn right 120
    end repeat
    turn right 30
    set color_index = color_index + 1
    if color_index == 3
        set color_index = 0
    end if
end repeat`,

  "Polygon Pattern": `# Draw polygons of 3 to 6 sides
set colors = ["red", "green", "blue", "purple"]
set color_index = 0

set sides = [3, 4, 5, 6]
set index = 0

repeat 4
    color colors[color_index]
    set n = sides[index]
    repeat n
        move forward 80
        turn right 360 / n
    end repeat
    turn right 30
    set color_index = color_index + 1
    set index = index + 1
end repeat`,

  /* 🔵 ADVANCED LEVEL (Patterns & Designs) */

  "Rainbow Spiral": `# Rainbow spiral pattern
set colors = ["red", "orange", "yellow", "green", "blue", "purple"]
set length = 5
set color_index = 0

repeat 75
    color colors[color_index]
    move forward length
    turn right 60
    set length = length + 2
    set color_index = color_index + 1
    if color_index == 6
        set color_index = 0
    end if
end repeat`,

  "Star Circle": `# Cody draws small stars in a circle
set colors = ["purple", "blue", "pink"]
set color_index = 0

repeat 6
    color colors[color_index]
    repeat 5
        move forward 100
        turn right 144
    end repeat
    turn right 60
    set color_index = color_index + 1
    if color_index == 3
        set color_index = 0
    end if
end repeat`,

  "Spiral Path": `# Cody makes a simple colorful spiral
set colors = ["red", "orange", "yellow", "green", "blue", "purple"]
set color_index = 0
set length = 10

repeat 60
    color colors[color_index]
    move forward length
    turn right 15
    set length = length + 2
    set color_index = color_index + 1
    if color_index == 6
        set color_index = 0
    end if
end repeat`,

  "Hexagon Flower": `# Cody draws a flower made of hexagons
set colors = ["red", "yellow", "green", "blue", "purple"]
set color_index = 0

repeat 6
    color colors[color_index]
    repeat 6
        move forward 70
        turn right 60
    end repeat
    turn right 60
    set color_index = color_index + 1
    if color_index == 5
        set color_index = 0
    end if
end repeat`
};
