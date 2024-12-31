# Changelog 
see [keepchangelog](https://keepachangelog.com/de/1.1.0/)

## [0.1.0] - 2024-11-19

### Initial version for github

## [0.2.0] - 2024-12-15

### Added

- added method for scanning from a reader

### Changed

- Switch license to apache 2.0
- changed logic for TokenReader. It has a fixed sized and tries to read as much tokens as possible
  before resizing the buffer or removing the accepted buffer

## [0.2.1] - 2024-12-15

### Fixed

- move Readme_tmp.md back to Readme.md

## [0.3.0] - 2024-12-30

### Added 

- added in TokenReader the methods getXPos and getYPos for reading the start position
  and getXEndPos and getYPos for reading the end position of the token.
  See example test5()

- added class Matcher for matching tokens. See example test5WithMatcher

## [0.3.1] - 2024-12-31

- changed descritpion of extLex