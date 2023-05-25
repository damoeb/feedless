# Filter Expressions

Feed items - also referred to as articles - can be filtered. `feedless` offers some basic operators to construct complex expressions.
To access article values use `#title`, `#url`, `#body` or `#any`

Strings have been wrapped by `"<STRING>"`. For examples take a look at `SimpleArticleFilterTest.kt`

## Examples
- title longer than 4 
```
gt(len(#title), 4)
```
- url not ending with `/comments` 
```
not(endsWith(#url, "/comments"))
```

## Operators

### EndsWith
```text
endsWith(value: string, suffix: string): boolean
```
Checks if `value` ends with the given `suffix` string.

*Example*
```text
endsWith(#url, "#comments")
```


### StartsWith
```text
startsWith(value: string, prefix: string): boolean
```
Checks if `value` starts with the given `prefix` string.

*Example*
```text
startsWith(#url, "https")
```


### Contains
```text
contains(haystack: string, needle: string): boolean
```
Checks if `needle` string is contained in `haystack` string.

*Example*
```text
contains("Whatever I say", "ever")
```


### Greater Than (Equals)
```text
gt(left: number, right: number): boolean
gte(left: number, right: number): boolean
```

### Lower Than (Equals)
```text
lt(left: number, right: number): boolean
lte(left: number, right: number): boolean
```

### Equals
```text
eq(left: number, right: number): boolean
```

### Or
```text
or(left: boolean, right: boolean): boolean
```

### And
```text
and(left: boolean, right: boolean): boolean
```

### Not
```text
not(value: boolean): boolean
```
